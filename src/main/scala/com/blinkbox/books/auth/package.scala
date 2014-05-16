package com.blinkbox.books

import com.blinkbox.books.auth.Elevation._
import scala.concurrent.Future

package object auth {
  type TokenDeserializer = String => Future[User]
  type TokenElevationChecker = String => Future[Elevation]
}
