package com.mle.push.adm

import com.mle.http.AsyncHttp
import com.mle.http.AsyncHttp.{RichRequestBuilder, _}
import com.mle.push.adm.AmazonMessaging._
import com.mle.push.android.AndroidMessage
import com.mle.push.{PushClient, MessagingClient, PushException}
import com.mle.util.Log
import com.mle.util.Utils.executionContext
import com.ning.http.client.Response
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

/**
 * @author Michael
 */
class AmazonMessaging(clientID: String, clientSecret: String) extends PushClient[AndroidMessage] with Log {
  def send(id: String, data: Map[String, String]): Future[Response] =
    send(id, AndroidMessage(data, expiresAfter = 60.seconds))

  def send(id: String, message: AndroidMessage): Future[Response] = {
    val body = Json.toJson(message)
    log.info(s"Sending body: ${Json.stringify(body)}")
    token(clientID, clientSecret).flatMap(t => {
      AsyncHttp.postJson(s"https://api.amazon.com/messaging/registrations/$id/messages", body, Map(
        AUTHORIZATION -> s"Bearer $t",
        AmazonTypeVersion -> AmazonTypeVersionValue,
        AmazonAcceptType -> AmazonAcceptTypeValue
      ))
    })
  }

  def token(clientID: String, clientSecret: String): Future[String] =
    accessToken(clientID, clientSecret).map(_.access_token)

  def accessToken(clientID: String, clientSecret: String): Future[AccessToken] =
    tokenRequest(clientID, clientSecret)
      .flatMap(response => Json.parse(response.getResponseBody).validate[AccessToken].fold(
      errors => Future.failed[AccessToken](new PushException(s"Invalid JSON in ADM response: $errors")),
      valid => Future.successful(valid)
    ))

  private def tokenRequest(clientID: String, clientSecret: String): Future[Response] = {
    AsyncHttp.execute(client => {
      client.post("https://api.amazon.com/auth/O2/token", "").addParameters(
        GRANT_TYPE -> CLIENT_CREDENTIALS,
        SCOPE -> MESSAGING_PUSH,
        CLIENT_ID -> clientID,
        CLIENT_SECRET -> clientSecret)
    }, Map(CONTENT_TYPE -> WWW_FORM_URL_ENCODED))
  }

}

object AmazonMessaging {
  val GRANT_TYPE = "grant_type"
  val CLIENT_CREDENTIALS = "client_credentials"
  val SCOPE = "scope"
  val MESSAGING_PUSH = "messaging:push"
  val CLIENT_ID = "client_id"
  val CLIENT_SECRET = "client_secret"
  val ACCESS_TOKEN = "access_token"

  val AmazonTypeVersion = "X-Amzn-Type-Version"
  val AmazonTypeVersionValue = "com.amazon.device.messaging.ADMMessage@1.0"
  val AmazonAcceptType = "X-Amzn-Accept-Type"
  val AmazonAcceptTypeValue = "com.amazon.device.messaging.ADMSendResult@1.0"

}
