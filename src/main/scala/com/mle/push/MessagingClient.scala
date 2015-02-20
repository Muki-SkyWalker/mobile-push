package com.mle.push

import com.mle.concurrent.FutureOps
import com.mle.util.Log
import com.ning.http.client.Response

import scala.concurrent.{ExecutionContext, Future}

/**
 *
 * @tparam T type of device
 */
trait MessagingClient[T] extends Log {
  def send(dest: T): Future[Response]

  def sendLogged(dest: T)(implicit ec: ExecutionContext): Future[Unit] = send(dest)
    .map(r => log info s"Sent message to: $dest. Response: ${r.getStatusText}")
    .recoverAll(t => log.warn(s"Unable to send message to: $dest", t))
}

/**
 *
 * @tparam T type of message
 */
trait PushClient[T, U] {
  def send(id: String, dest: T): Future[U]
}

trait HttpPushClient[T] extends PushClient[T, Response]
