package com.worrel.scathe.models

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.velvia.msgpack.RawStringCodecs.StringCodec
import org.velvia.msgpack.TransformCodecs.TransformCodec

/**
  * Scala's built-in Enumeration type is not great sadly.  This is a simple custom approach.
  */
object DataEventTypes {
  sealed abstract class DataEventType(val code: String)

  case object DataReceived extends DataEventType("DATA_RECEIVED")
  case class Unknown(unkCode: String) extends DataEventType("UNKNOWN")

  val dataEvents = Seq(DataReceived)
  val eventCodeMap = dataEvents.map(e => e.code -> e).toMap
  implicit val msgPackCodec = new TransformCodec[DataEventType, String](_.code,str => eventCodeMap.getOrElse(str, Unknown(str)))

  /**
    * Encode this enum-like object as its String code value in JSON
    */
  object JsonCodec extends Encoder[DataEventType] with Decoder[DataEventType] {
    override def apply(e: DataEventType): Json = Encoder.encodeString(e.code)
    override def apply(c: HCursor): Result[DataEventType] = Decoder.decodeString.emap(str => eventCodeMap.get(str).toRight(str)).apply(c)
  }
}
