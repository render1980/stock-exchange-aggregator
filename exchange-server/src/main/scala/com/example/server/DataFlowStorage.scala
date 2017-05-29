package com.example.server

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.FiniteDuration
import concurrent.ExecutionContext

import akka.actor.{Actor, ActorLogging}

object DataFlowStorage {

  case class Put(record: String)

  case class Get(duration: FiniteDuration)

}

class DataFlowStorage extends Actor with ActorLogging {

  implicit val ec = context.system.dispatcher

  val recordsAmount = 10

  import DataFlowStorage._

  var dataFlow = new ArrayBuffer[String]


  override def preStart(): Unit = {
    dataFlow ++= Seq("1a", "2b", "3c")
  }

  override def receive = {
    case Put(record)   ⇒
      dataFlow ++ record
    case Get(duration) ⇒
      val currentRecords = dataFlow.take(2).toString()
      //      Future {
      //        currentRecords
      //      }.pipeTo(sender)
      val aggregator = sender()
      aggregator ! currentRecords
    case _             ⇒
      log.warning(s"Unexpected event")
  }
}
