package com.blinkbox.books.auth

import com.blinkbox.security.jwt.{InvalidTokenException, UnsupportedTokenException, TokenDecoder}
import com.blinkbox.security.jwt.encryption.{RSA_OAEP, A128GCM, EncryptionMethod}
import com.blinkbox.security.jwt.signatures.{ES256, SignatureAlgorithm}
import java.security._
import java.security.spec.{X509EncodedKeySpec, InvalidKeySpecException, PKCS8EncodedKeySpec}
import java.io.IOException
import java.nio.file.{Paths, Files}
import scala.collection.concurrent.TrieMap

class ZuulTokenDecoder(val keysFolder: String) extends TokenDecoder {
  val privateKeys = new TrieMap[String, Array[Byte]]()
  val publicKeys = new TrieMap[String, Array[Byte]]()

  override def getDecrypter(header: java.util.Map[String, AnyRef]): EncryptionMethod =
    if (header.get("enc") == A128GCM.NAME && header.get("alg") == RSA_OAEP.NAME) {
      header.get("kid") match {
        case keyId: String => new A128GCM(new RSA_OAEP(privateKey(keyId)))
        case _ => super.getDecrypter(header)
      }
    } else super.getDecrypter(header)

  override def getVerifier(header: java.util.Map[String, AnyRef]): SignatureAlgorithm =
    if (header.get("alg") == ES256.NAME) {
      header.get("kid") match {
        case keyId: String => new ES256(publicKey(keyId))
        case _ => super.getVerifier(header)
      }
    } else super.getVerifier(header)

  private def privateKey(keyId: String): PrivateKey = try {
    val keyData = getKeyData(keyId, "private.key", cache = privateKeys)
    KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyData))
  } catch {
    case e: NoSuchAlgorithmException => throw new UnsupportedTokenException("The RSA encryption algorithm is not supported.", e)
    case e: InvalidKeySpecException => throw new InvalidKeyException(s"The private key '$keyId' is invalid.", e)
  }

  private def publicKey(keyId: String): PublicKey = try {
    val keyData = getKeyData(keyId, "public.key", cache = publicKeys)
    KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(keyData))
  } catch {
    case e: NoSuchAlgorithmException => throw new UnsupportedTokenException("The ECDSA signature algorithm is not supported.", e)
    case e: InvalidKeySpecException => throw new InvalidKeyException(s"The public key '$keyId' is invalid.", e)
  }

  private def getKeyData(keyId: String, name: String, cache: TrieMap[String, Array[Byte]]): Array[Byte] =
    cache.getOrElseUpdate(keyId, {
      try {
        Files.readAllBytes(Paths.get(keysFolder, keyId, name))
      } catch {
        case e: IOException => throw new InvalidTokenException(s"Invalid key identifier '$keyId'.")
      }
    })
}
