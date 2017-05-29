package com.example.server

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.FiniteDuration

import akka.actor.{Actor, ActorLogging}

object DataFlowStorage {

  case class Put(record: String)

  case class Get(duration: FiniteDuration)

  case class Record(
    ticker: String, timestamp: String,
    open: Float, high: Float,
    low: Float, close: Float, volume: Int
  )

}

class DataFlowStorage extends Actor with ActorLogging {

  implicit val ec = context.system.dispatcher

  val recordsAmount = 10

  import DataFlowStorage._

  var dataFlow = new ArrayBuffer[Record]


  override def preStart(): Unit = {
    dataFlow ++= Seq(
      Record("AAPL", "2016-01-01T15:02:00Z", 101.1f, 101.3f, 101, 101, 1300),
      Record("AAPL", "2016-01-01T15:02:00Z", 102.1f, 101.3f, 102, 102, 1400)
    )
  }

  override def receive = {
    case Put(record)   ⇒
      dataFlow ++ record
    case Get(duration) ⇒
      val currentRecords = dataFlow.take(recordsAmount).toString
      val aggregator = sender()
      log.debug(s"DataFlowStorage actor received Get($duration) from ${aggregator.path}")
      log.debug(s"Sending $currentRecords from DataFlowStorage to ${aggregator.path}")
      aggregator ! currentRecords
    case _             ⇒
      log.warning(s"Unexpected event")
  }
}
