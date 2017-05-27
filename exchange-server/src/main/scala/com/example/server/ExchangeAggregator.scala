package com.example.server

import akka.actor.{Actor, ActorLogging}
import akka.io.Tcp.{ConnectionClosed, Received, Write}
import akka.util.ByteString

class ExchangeAggregator extends Actor with ActorLogging {

  private val resp =
    s"""
       |{"ticker": "AAPL",
       |"timestamp": "2016-01-01T15:02:00Z",
       |"open": 101.1,
       |"high": 101.3,
       |"low": 101,
       |"close": 101,
       |"volume": 1300}""".stripMargin

  override def receive = {
    case Received(data)          ⇒
      sender() ! Write(ByteString(resp))
    case _: ConnectionClosed ⇒
      context.stop(self)
  }
}
