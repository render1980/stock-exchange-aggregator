package server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}

class Server(host: String, port: String) extends Actor {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 9999))

  override def receive = {
    case b@Bound(localAddress)      ⇒
      context.parent ! b
    case CommandFailed(_: Bind)     ⇒ context stop self
    case c@Connected(remote, local) ⇒
      val handler = context.actorOf(Props[ExchangeAggregator])
      val connection = sender()
      connection ! Register(handler)
  }
}
