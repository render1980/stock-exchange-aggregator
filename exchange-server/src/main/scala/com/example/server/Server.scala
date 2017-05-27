package com.example.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.io.Tcp._

class Server(host: String, port: String) extends Actor with ActorLogging {

  implicit val system = ActorSystem()
  implicit val ec     = system.dispatcher

  override def preStart {
    log.debug(s"calling Server preStart()")
    IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 9999))
  }

  override def receive = {
    case b@Bound(localAddress)      ⇒
      log.debug(s"Bound to $localAddress")
      context.parent ! b
    case CommandFailed(b: Bind)     ⇒
      log.error(s"CommandFailed: $b")
      context stop self
    case c@Connected(remote, local) ⇒
      log.debug(s"Connected to $remote from $local")
      val handler = context.actorOf(Props[ExchangeAggregator])
      sender ! Register(handler)
  }
}
