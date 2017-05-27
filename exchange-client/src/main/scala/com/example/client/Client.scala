package com.example.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging}
import akka.io.Tcp._
import akka.io.{IO, Tcp}

class Client(remote: InetSocketAddress) extends Actor with ActorLogging {

  import context.system

  override def preStart(): Unit = {
    IO(Tcp) ! Connect(remote)
  }

  override def receive: Receive = {
    case CommandFailed(_: Connect) ⇒
      //      listener ! "connect failed"
      log.error(s"Connection with $remote have failed!")
      context stop self
    case c@Connected(_, _)         ⇒ {
      //      listener ! c
      log.debug(s"Connection with $remote succeeded!")
      sender ! Register(self)

      context become {
        case CommandFailed(w: Write) ⇒
          // O/S buffer was full
          log.error(s"Writing to buffer have failed!")
        case Received(data)          ⇒
          log.debug(s"Received $data from $remote")
        case "close"                 ⇒
          log.debug(s"Received Close command")
          sender ! Close
        case _: ConnectionClosed     ⇒
          //          listener ! "connection closed"
          log.debug(s"Connection closed by server")
          context stop self
      }
    }
    case _                         ⇒
      log.warning(s"Unknown event")
  }
}
