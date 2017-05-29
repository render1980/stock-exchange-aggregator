package com.example.server

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Main extends App {
  val config       = ConfigFactory.load()
  val host         = config.getString("host")
  val port         = config.getString("port")
  val upstreamHost = config.getString("upstream.host")
  val upstreamPort = config.getInt("upstream.port")

  val system = ActorSystem("system")

  //  system.actorOf(
  //    Props(new UpstreamClient(
  //      new InetSocketAddress(upstreamHost, upstreamPort)
  //    )), "upstream-client"
  //  )

  system.actorOf(
    Props(new Server(host, port)), "server"
  )
//
//  system.actorOf(
//    Props(new DataFlowStorage()), "data-flow-storage"
//  )

//  system.actorOf(
//    Props(new ExchangeAggregator()), "exchange-aggregator"
//  )
}
