package com.worrel.scathe.services.impl

import java.io.ByteArrayInputStream

import com.worrel.scathe.database.ConnectionPool
import com.worrel.scathe.models.DataTags.TagMap
import com.worrel.scathe.services.StorageHandler
import scalikejdbc._

import scala.util.Try

trait PostgresStorageHandler extends StorageHandler {

  def init(): Unit = {
    DB autoCommit { implicit session =>
      sql"CREATE TABLE data_point (id SERIAL, occurred TIMESTAMP NOT NULL, seq_num BIGINT, episode_num BIGINT, data BLOG)".execute().apply()
    }
  }

  override def store(accountId: String, stream: String, tags: TagMap,
                     dataPoints: => Seq[(Long, Option[Long], Option[Long], Array[Byte])]): Seq[Try[String]] = {
    DB autoCommit  { implicit session =>
      dataPoints.map {
        case (occurred, optSeqNum, optEpisodeNum, data) =>

          val dataStream = new ByteArrayInputStream(data)

          Try {
            sql"""INSERT INTO data_point (occurred, seq_num, episode_num, data)
                 VALUES (to_timestamp(${occurred}), ${optSeqNum}, ${optEpisodeNum}, ${dataStream})""".updateAndReturnGeneratedKey().apply()
          }.map(_.toString)
      }
    }
  }

  override def retrieve(ids: Seq[String]): Seq[Option[(Long, Option[Long], Option[Long], Array[Byte])]] = ???
}
