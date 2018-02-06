package com.worrel.scathe

import akka.actor.ActorSystem
import com.worrel.scathe.services.EventConsumer
import com.worrel.scathe.services.impl.RabbitEventService


trait Backend {
  self: EventConsumer =>

  val actorSystem = ActorSystem("backend-server")

  def init() = {



    subscribeToDataEvents { event =>
      println(s"Received: ${event}")
      true
    }
  }
}

object BackendNode extends App
  with Backend
  with RabbitEventService
  with RabbitQueueConfig {

  init
}