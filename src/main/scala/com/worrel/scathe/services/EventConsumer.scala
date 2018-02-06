package com.worrel.scathe.services

import com.worrel.scathe.models.Messages.DataEvent

trait EventConsumer {
  trait ConsumerRef {
    def stop
  }

  def subscribeToDataEvents(onEvent: DataEvent => Boolean): ConsumerRef
}
