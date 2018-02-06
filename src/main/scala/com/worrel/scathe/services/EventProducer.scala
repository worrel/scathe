package com.worrel.scathe.services

import com.worrel.scathe.models.Messages.PlatformEvent

trait EventProducer {
  def emitEvent(accountId: String, event: PlatformEvent)
}
