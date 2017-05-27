package com.example.client

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory


object Main extends App {
  val config = ConfigFactory.load()
  val serverHost = config.getString("server.host")
  val serverPort = config.getString("server.port")

  val inetSocketAddr = new InetSocketAddress("localhost", 9999)

  ActorSystem().actorOf(
    Props(new Client(inetSocketAddr))
  )

}
