package com.example.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString

object Client {
  val ack = "ACK"

  case object MyAck extends Event

}

class Client(remote: InetSocketAddress) extends Actor with ActorLogging {

  import Client._
  import context.system

  override def preStart(): Unit = {
    IO(Tcp) ! Connect(remote)
  }

  override def receive: Receive = {
    case CommandFailed(_: Connect) ⇒
      log.error(s"Connection with $remote have failed!")
      context stop self
    case c@Connected(_, _)         ⇒ {
      log.debug(s"Connection with $remote succeeded!")
      sender ! Register(self)
      // send request to server
      sender() ! Write(ByteString(ack), MyAck)

      context become {
        case CommandFailed(w: Write) ⇒
          // O/S buffer was full
          log.error(s"Writing to buffer have failed!")
        case Received(data)          ⇒
          val decodedData = data.decodeString(ByteString.UTF_8)
          log.debug(s"Received $decodedData from $remote")
        case "close"                 ⇒
          log.debug(s"Received Close command")
          sender ! Close
        case _: ConnectionClosed     ⇒
          log.debug(s"Connection closed by server")
          context stop self
      }
    }
    case _                         ⇒
      log.warning(s"Unknown event")
  }
}
