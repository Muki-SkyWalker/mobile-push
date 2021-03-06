package com.malliina.push.adm

import com.malliina.json.JsonFormats
import play.api.libs.json.Json

import scala.concurrent.duration.Duration

case class AccessToken(access_token: String,
                       expires_in: Duration,
                       scope: String,
                       token_type: String)

object AccessToken {
  implicit val durationFormat = JsonFormats.durationFormat
  implicit val json = Json.format[AccessToken]
}
