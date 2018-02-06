package com.worrel.scathe.models

import com.worrel.scathe.models.DataTags.TagMap
import com.worrel.scathe.models.DataEventTypes.DataEventType

object Messages {
  abstract trait EventHeader[T] {
    def eventType: T
    val accountId: String
  }

  sealed abstract trait PlatformEvent

  case class DataEventHeader(eventType: DataEventType,
                             accountId: String) extends EventHeader[DataEventType]

  case class DataEvent(header: DataEventHeader,
                       stream: String,
                       tags: TagMap,
                       count: Long) extends PlatformEvent {

  }
}
