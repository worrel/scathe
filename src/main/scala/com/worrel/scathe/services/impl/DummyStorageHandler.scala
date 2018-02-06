package com.worrel.scathe.services.impl

import com.worrel.scathe.models.DataTags.TagMap
import com.worrel.scathe.services.StorageHandler
import com.worrel.scathe.services.StorageHandler

import scala.util.{Success, Try}

trait DummyStorageHandler extends StorageHandler {

  // not recommended!
  var counter = 0

  def store(accountId: String, stream: String, tags: TagMap, dataPoints: => Seq[DataPoint]): Seq[Try[String]] = {
    dataPoints.map { _ =>
      val idNum = counter
      counter += 1
      Success(s"${stream}_${idNum}")
    }
  }
}
