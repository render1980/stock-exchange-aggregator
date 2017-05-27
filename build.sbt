name := "stock-exchange-aggregator"

version := "1.0"

organization := "com.example"

lazy val client = project.in(file("exchange-client"))

lazy val server = project.in(file("exchange-server"))
