package com.worrel.scathe.services

import com.worrel.scathe.models.DataTags.TagMap

import scala.util.Try

abstract trait StorageHandler {
  type DataPoint = (Long, Option[Long], Option[Long], Array[Byte])
  def store(accountId: String, stream: String, tags: TagMap, dataPoints: => Seq[DataPoint]): Seq[Try[String]]

  def retrieve(ids: Seq[String]): Seq[Option[DataPoint]]
}
