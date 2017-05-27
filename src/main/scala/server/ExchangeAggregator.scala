package server

import akka.actor.{Actor, ActorLogging}

class ExchangeAggregator extends Actor with ActorLogging {

  import akka.io.Tcp._

  override def receive = {
    case Received(data) ⇒ sender() ! Write(data)
    case PeerClosed     ⇒ context stop self
  }
}
