package com.example.server

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.io.Tcp.{ConnectionClosed, Received, Write}
import akka.util.ByteString
import akka.pattern.ask

class ExchangeAggregator extends Actor with ActorLogging {

  implicit val system = ActorSystem()
  implicit val ec     = system.dispatcher

  private val resp =
    s"""{"ticker": "AAPL","timestamp": "2016-01-01T15:02:00Z","open": 101.1,"high": 101.3,"low": 101,"close": 101,"volume": 1300}"""

  private val resp2 =
    s"""{"ticker": "AAPL","timestamp": "2016-01-01T15:02:25Z","open": 101.2,"high": 101.3,"low": 100,"close": 101,"volume": 500}"""

  override def receive = {
    case Received(data)      ⇒
      // Send minutes `candles` for last 10 minutes
      log.info(s"Send $data to client")

      // Ask actor which can get last records
      // and process future

//      self ? "Tick" onComplete {
//        case Success(res) ⇒ res
//        case Failure(e) ⇒ {
//
//        }
//      }

      sender ! Write(ByteString(resp))
      // Schedule to send `candles` each minute
      system.scheduler.schedule(
        Duration.create(5, TimeUnit.SECONDS),
        Duration.create(5, TimeUnit.SECONDS),
        sender,
        Write(ByteString(resp2))
      )
    case _: ConnectionClosed ⇒
      context.stop(self)
  }
}
