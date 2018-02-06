package com.worrel.scathe.services.impl

import java.util.Base64

import com.worrel.scathe.models.DataEventTypes.DataReceived
import com.worrel.scathe.models.DataTags.TagMap
import com.worrel.scathe.models.JSON.Schema.{ExperienceData, ExperienceDataBatch}
import com.worrel.scathe.models.Messages.{DataEvent, DataEventHeader}
import com.worrel.scathe.services.{EventProducer, ExperienceHandler, StorageHandler}
import com.typesafe.scalalogging.LazyLogging
import com.worrel.scathe.models.DataEventTypes.DataReceived
import com.worrel.scathe.models.DataTags.TagMap
import com.worrel.scathe.models.JSON.Schema.{ExperienceData, ExperienceDataBatch}
import com.worrel.scathe.services.{EventProducer, ExperienceHandler, StorageHandler}

import scala.util.{Failure, Success, Try}

trait EventingExperienceHandler extends ExperienceHandler
  with LazyLogging {

  self: EventProducer with StorageHandler =>

  private type ResultPairs = Stream[(ExperienceData, StoreResult)]
  private type GroupKey = (String, TagMap)
  private case class GroupResults(key: GroupKey, attempted: ResultPairs, invalid: ResultPairs)

  private case class TaggedData(data: ExperienceData, stream: String, tags: Option[TagMap])

  def storeExperienceData(batch: ExperienceDataBatch): Map[ExperienceData,StoreResult] = {

    val batchTags = batch.tags.map(TagMap.fromJsonObject)

    // merge per-point and per-batch tags
    def mergeBatchInfo(expData: ExperienceData) =
      TaggedData(
        expData,
        expData.stream.getOrElse(batch.stream),
        expData.tags
          .map(TagMap.fromJsonObject)
          .flatMap(t1 => batchTags.map(_ ++ t1))
          .orElse(batchTags)
      )

    def decodePoints(dataPoints: Stream[TaggedData]) =
      dataPoints.map { td => (td.data, Try(Base64.getDecoder.decode(td.data.b64Data))) }

    def fireEvent(groupResults: GroupResults): Unit = {
      val (successes, failures) = groupResults.attempted.partition {
        case (_,DataStored(_)) => true
        case _ => false
      }

      val successCount = successes.size
      val failCount = failures.size

      if (successCount > 0) {
        logger.info(s"Firing events for stored points: {}/{}", successCount, successCount + failCount)

        emitEvent(
          batch.accountId,
          DataEvent(
            DataEventHeader(DataReceived, batch.accountId),
            groupResults.key._1,
            groupResults.key._2,
            successCount))
      }
    }

    // store mini-batch of points with same stream & tags
    def storeGroup(group: ((String, TagMap), Stream[TaggedData])): GroupResults = {
      group match {
        case (key @ (stream,tags), dataPoints) =>

          val (validPoints, inValidPoints) = decodePoints(dataPoints).partition(_._2.isSuccess)

          // convert to raw tuple format expected by StorageHandler
          val pointsToStore = validPoints.collect {
            case (ed, Success(data)) => (ed.timestamp, ed.sequenceNumber, ed.episodeNumber, data)
          }

          val storeResults = store(batch.accountId, stream, tags, pointsToStore)

          val attempted: ResultPairs = validPoints.zip(storeResults).map {
            case ((ed, _), result) => ed -> (result.fold(StoreFailed, DataStored): StoreResult)
          }

          val invalid: ResultPairs = inValidPoints.collect {
            case (ed, Failure(_)) => ed -> (InvalidData: StoreResult)
          }

          GroupResults(key, attempted, invalid)
      }
    }

    val results = batch.data.toStream
      .map(mergeBatchInfo)
      .groupBy(tagged => (tagged.stream, tagged.tags.getOrElse(TagMap.empty)))
      .map(storeGroup)

    results.foreach(fireEvent)

    results.foldLeft(Map.empty[ExperienceData,StoreResult]) {
      (res, group) => res ++ group.attempted ++ group.invalid
    }
  }

}
