package com.blinkbox.books.auth

import com.blinkbox.security.jwt.{InvalidClaimException, InvalidTokenException, UnsupportedTokenException, TokenDecoder}
import com.blinkbox.security.jwt.encryption.{RSA_OAEP, A128GCM, EncryptionMethod}
import com.blinkbox.security.jwt.signatures.{ES256, SignatureAlgorithm}
import java.io.IOException
import java.nio.file.{Paths, Files}
import java.security.spec.{X509EncodedKeySpec, InvalidKeySpecException, PKCS8EncodedKeySpec}
import java.security.{PublicKey, PrivateKey, NoSuchAlgorithmException, KeyFactory}
import scala.collection.concurrent.TrieMap
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.Some

class ZuulTokenDeserializer(decoder: TokenDecoder)(implicit val executionContext: ExecutionContext) extends TokenDeserializer {
  private val UserUrn = """urn:blinkbox:zuul:user:(\d+)""".r
  private val ClientUrn = """urn:blinkbox:zuul:client:(\d+)""".r

  override def apply(token: String): Future[User] = Future(decoder.decode(token)) flatMap {
    case claims: java.util.Map[_, _] => fromClaims(claims.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap)
    case _ => Future.failed(new InvalidTokenException("The token does not contain valid claims"))
  }

  private def fromClaims(claims: Map[String, AnyRef]): Future[User] = Future { User(userId(claims), clientId(claims)) }

  private def userId(claims: Map[String, AnyRef]): Int = {
    claims.get("sub") match {
      case Some(UserUrn(id)) => id.toInt
      case _ => throw new InvalidClaimException("The token does not contain a valid user id claim.")
    }
  }

  private def clientId(claims: Map[String, AnyRef]): Option[Int] = {
    claims.get("bb/cid") map {
      case ClientUrn(id) => id.toInt
      case _ => throw new InvalidClaimException("The token does not contain a valid client id claim.")
    }
  }
}

class ZuulTokenDecoder(val keysFolder: String) extends TokenDecoder {
  val privateKeys = new TrieMap[String, Array[Byte]]()
  val publicKeys = new TrieMap[String, Array[Byte]]()

  override def getDecrypter(header: java.util.Map[String, AnyRef]): EncryptionMethod = {
    val decrypter = (header.get("enc"), header.get("alg")) match {
      case (A128GCM.NAME, RSA_OAEP.NAME) => header.get("kid") match {
        case keyId: String => Some(new A128GCM(new RSA_OAEP(privateKey(keyId))))
        case _ => None
      }
      case _ => None
    }

    decrypter match {
      case Some(d) => d
      case None => super.getDecrypter(header)
    }
  }

  override def getVerifier(header: java.util.Map[String, AnyRef]): SignatureAlgorithm = {
    val verifier = header.get("alg") match {
      case ES256.NAME => header.get("kid") match {
        case keyId: String if keyId != null => Some(new ES256(publicKey(keyId)))
        case _ => None
      }
      case _ => None
    }

    verifier match {
      case Some(v) => v
      case None => super.getVerifier(header)
    }
  }

  private def privateKey(keyId: String): PrivateKey = try {
    val keyData = getKeyData(keyId, "private.key", cache = privateKeys)
    KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyData))
  } catch {
    case e: NoSuchAlgorithmException => throw new UnsupportedTokenException("The encryption algorithm is not supported.", e)
    case e: InvalidKeySpecException => throw new InvalidTokenException("The key is invalid", e)
  }

  private def publicKey(keyId: String): PublicKey = try {
    val keyData = getKeyData(keyId, "public.key", cache = publicKeys)
    KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(keyData))
  } catch {
    case e: NoSuchAlgorithmException => throw new UnsupportedTokenException("The encryption algorithm is not supported.", e)
    case e: InvalidKeySpecException => throw new InvalidTokenException("The key is invalid", e)
  }

  private def getKeyData(keyId: String, name: String, cache: TrieMap[String, Array[Byte]]): Array[Byte] =
    cache.get(keyId) match {
      case Some(key) => key
      case None => try {
        val bytes = Files.readAllBytes(Paths.get(keysFolder, keyId, name))
        cache.putIfAbsent(keyId, bytes)
        bytes
      } catch {
        // TODO: Is this actually an internal server error if it doesn't have the key?
        case e: IOException => throw new InvalidTokenException("Invalid encryption key identifier")
      }
    }
}
