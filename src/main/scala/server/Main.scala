package server

import com.typesafe.config.ConfigFactory

object Main extends App {
  val config = ConfigFactory.load()
  val host = config.getString("host")
  val port = config.getString("port")

  val server = new Server(host, port)

}
