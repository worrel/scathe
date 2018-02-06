package com.worrel.scathe

trait RabbitQueueConfig {
  val apiEventRoutingKey = "api_event"
  val continuaExchange = "scathe.direct"
  val apiEventsQueue = "scathe.events"
}
