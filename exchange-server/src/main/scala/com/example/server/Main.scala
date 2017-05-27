package com.example.server

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Main extends App {
  val config = ConfigFactory.load()
  val host = config.getString("host")
  val port = config.getString("port")

  ActorSystem().actorOf(
    Props(new Server(host, port))
  )

}
