package com.example.server

import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.{ConnectionClosed, Received, Write}
import akka.pattern.ask
import akka.util.{ByteString, Timeout}
import com.example.server.DataFlowStorage.Get
import com.example.server.ExchangeAggregator.GetRecords

import concurrent.ExecutionContext

object ExchangeAggregator {

  case class GetRecords(recipient: ActorRef, storage: ActorRef)

}

class ExchangeAggregator extends Actor with ActorLogging {

  implicit val ec      = context.system.dispatcher
  implicit val timeout = Timeout(2 seconds)

  //  private val resp =
  //    s"""{"ticker": "AAPL","timestamp": "2016-01-01T15:02:00Z","open": 101.1,"high": 101.3,"low": 101,"close": 101,"volume": 1300}"""
  //
  //  private val resp2 =
  //    s"""{"ticker": "AAPL","timestamp": "2016-01-01T15:02:25Z","open": 101.2,"high": 101.3,"low": 100,"close": 101,"volume": 500}"""

  override def receive = {
    case Received(data)                 ⇒ {
      // Send minutes `candles` for last 10 minutes
      log.info(s"Actor path = ${self.path.toString} Received $data from client")
      // Ask actor which can get last records
      //      val storage = context.system.actorSelection("data-flow-storage").resolveOne()
      val storage = context.actorOf(Props[DataFlowStorage])
      storage ? Get(10 minutes) onComplete {
        case Success(res) ⇒
          log.debug(s"Sending $res to ${sender()}")
          sender ! Write(ByteString(res.asInstanceOf[String]))
          // Schedule to send `candles` each minute
          context.system.scheduler.scheduleOnce(
            10 seconds,
            self,
            GetRecords(sender, storage)
          )
        case Failure(e)   ⇒
          log.error(s"Data flow storage reading error: ${e.toString}")
      }
    }
    case GetRecords(recipient, storage) ⇒
      //      val storage = context.system.actorOf(Props[DataFlowStorage])
      storage ? Get(1 minutes) onComplete {
        case Success(res) ⇒
          recipient ! Write(ByteString(res.asInstanceOf[String]))
          context.system.scheduler.schedule(
            5 seconds,
            5 seconds,
            self,
            GetRecords(recipient, storage)
          )
        case Failure(e)   ⇒
          log.error(s"Data flow storage reading error: ${e.toString}")
      }
    case _: ConnectionClosed            ⇒
      context.stop(self)
  }
}
