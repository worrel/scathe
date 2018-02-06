package com.worrel.scathe.models

import cats.effect.IO
import JSON.Schema.ExperienceDataBatch
import io.circe.generic.auto._
import io.circe.JsonObject
import org.http4s.circe._

object JSON {

  object Schema {

    case class ExperienceData(timestamp: Long,
                              sequenceNumber: Option[Long],
                              episodeNumber: Option[Long],
                              tags: Option[JsonObject],
                              stream: Option[String],
                              b64Data: String)

    sealed trait APIMessage

    case class APIError(message: String) extends APIMessage

    case class ExperienceDataBatch(accountId: String,
                                   stream: String,
                                   tags: Option[JsonObject],
                                   data: Seq[ExperienceData]) extends APIMessage
  }

  object Codecs {
    implicit val EDBatchDecoder = jsonOf[IO,ExperienceDataBatch]
  }

}
