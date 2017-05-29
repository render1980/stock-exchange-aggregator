package com.example.server

import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.{ConnectionClosed, Received, Write}
import akka.pattern.ask
import akka.util.{ByteString, Timeout}
import com.example.server.DataFlowStorage.Get
import com.example.server.ExchangeAggregator.GetRecords


object ExchangeAggregator {

  case class GetRecords(recipient: ActorRef, storage: ActorRef)

}

class ExchangeAggregator extends Actor with ActorLogging {

  implicit val ec      = context.system.dispatcher
  implicit val timeout = Timeout(2 seconds)

  //  private val resp =
  //    s"""{"ticker": "AAPL","timestamp": "2016-01-01T15:02:00Z","open": 101.1,"high": 101.3,"low": 101,"close": 101,"volume": 1300}"""

  override def receive = {
    case Received(data)                 ⇒
      val client = sender()
      log.info(s"Actor path = ${self.path.toString} Received $data from client")
      // Ask actor which can get last records
      val storage = context.actorOf(Props[DataFlowStorage])
      // At first connection we need `candles` for last 10 minutes
      storage ? Get(10 minutes) onComplete {
        case Success(res) ⇒
          client ! Write(ByteString(res.asInstanceOf[String]))
          // Schedule to send `candles` each minute
          context.system.scheduler.schedule(
            5 seconds,
            5 seconds,
            self,
            GetRecords(client, storage)
          )
        case Failure(e)   ⇒
          log.error(s"Data flow storage reading error: ${e.getStackTrace}")
      }
    case GetRecords(recipient, storage) ⇒
      storage ? Get(1 minutes) onComplete {
        case Success(res) ⇒
          recipient ! Write(ByteString(res.asInstanceOf[String]))
        case Failure(e)   ⇒
          log.error(s"Data flow storage reading error: ${e.getStackTrace}")
      }
    case _: ConnectionClosed            ⇒
      context.stop(self)
  }
}
