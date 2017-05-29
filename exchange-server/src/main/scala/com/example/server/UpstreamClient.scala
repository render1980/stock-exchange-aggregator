package com.example.server

import java.net.InetSocketAddress
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.{ByteString, Timeout}
import com.example.server.DataFlowStorage.Put

object UpstreamClient {
  val ack = "ACK"

  case object UpstreamAck extends Event

}

class UpstreamClient(upstream: InetSocketAddress) extends Actor with ActorLogging {

  import UpstreamClient._

  implicit val system  = ActorSystem()
  implicit val timeout = Timeout(5 seconds)
  implicit val ec      = system.dispatcher

  override def preStart(): Unit = {
    IO(Tcp) ! Connect(upstream)
  }

  override def receive: Receive = {
    case CommandFailed(_: Connect) ⇒
      log.error(s"Connection with $upstream have failed!")
      context stop self
    case c@Connected(_, _)         ⇒ {
      log.debug(s"Connection with $upstream succeeded!")
      sender ! Register(self)
      // send request to server
      sender() ! Write(ByteString(ack), UpstreamAck)

      context become {
        case CommandFailed(w: Write) ⇒
          // O/S buffer was full
          log.error(s"Writing to buffer have failed!")
        case Received(data)          ⇒
          val decodedData = data.decodeString(ByteString.UTF_8)
          log.debug(s"Received $decodedData from $upstream")
          // PUT to storage
          context.actorSelection("data-flow-storage").resolveOne() onComplete {
            case Success(storage) ⇒
              storage ! Put(data.decodeString("UTF-8"))
            case Failure(e)       ⇒
              log.error(s"Get DataFlowStorage ActorRef error: ${e.toString}")
          }

        case "close"             ⇒
          log.debug(s"Received Close command from $upstream")
          sender ! Close
        case _: ConnectionClosed ⇒
          //          listener ! "connection closed"
          log.debug(s"Connection closed by server ($upstream)")
          context stop self
      }
    }
    case _                         ⇒
      log.warning(s"Unknown event")
  }
}
