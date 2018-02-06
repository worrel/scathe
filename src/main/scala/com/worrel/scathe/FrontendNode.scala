package com.worrel.scathe

import cats.effect.IO
import cats.implicits._
import fs2.StreamApp
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import com.worrel.scathe.models.JSON.Schema._
import com.worrel.scathe.models.JSON.Codecs._
import akka.actor.ActorSystem
import com.worrel.scathe.models.DataEventTypes.DataReceived
import com.worrel.scathe.models.DataTags.StringTag
import com.worrel.scathe.models.Messages.{DataEvent, DataEventHeader}
import com.worrel.scathe.services.{EventProducer, ExperienceHandler}
import com.worrel.scathe.services.impl.{DummyStorageHandler, EventingExperienceHandler, PostgresStorageHandler, RabbitEventService}
import com.typesafe.scalalogging.LazyLogging
import com.worrel.scathe.services.{EventProducer, ExperienceHandler}
import org.http4s.server.Router

trait FrontendAPI extends Http4sDsl[IO] with LazyLogging {

  self: ExperienceHandler with EventProducer =>

  val actorSystem = ActorSystem("api-server")

  val v1service = HttpService[IO] {

    case GET -> Root / "events" / "generate" / IntVar(count) => {
      (1 to count).par.foreach { num =>
        logger.debug("Generating {} events",num)

        emitEvent("123",
          DataEvent(
            header = DataEventHeader(DataReceived, "123"),
            stream = "yolo",
            tags = Map("location"-> StringTag("kitchen")),
            count =1))
      }

      Ok()
    }

    case req @ POST -> Root / "experiences" =>
      req.as[ExperienceDataBatch].flatMap {
        batch =>
          val results = storeExperienceData(batch)
          val orderedResults = batch.data.map(results(_))

          val externalResults = orderedResults.map {
            case DataStored(id) => JsonObject("id" -> id.asJson)
            case InvalidData => JsonObject("err" -> "Invalid binary data encoding".asJson)
            case StoreFailed(err) =>  JsonObject("err" -> s"Storage failure: ${err.getMessage}".asJson)
          }

          logger.debug("Stored {} events", results.size)

          Ok(externalResults.asJson)
      }
  }

  val v2service = HttpService[IO] {
    case req @ GET -> _ =>
      logger.warn("Call to unimplemented API endpoint: {}", req)
      Ok(APIError("Not yet done with this bit, sorry").asJson)
  }

  val apiServices = Router[IO](
    "/v1" -> v1service,
    "/v2" -> v2service
  )
}

object FrontendNode extends StreamApp[IO]
  with FrontendAPI
  with EventingExperienceHandler
  with RabbitEventService
  with RabbitQueueConfig {

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(apiServices, "/api")
      .serve

}
