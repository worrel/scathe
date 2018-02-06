package com.worrel.scathe.models

import java.io.{DataInputStream, DataOutputStream}

import io.circe.JsonObject
import org.velvia.msgpack.{Codec, FastByteMap, Format, RawStringCodecs}

object DataTags {
  sealed abstract trait TagValue
  case class StringTag(value: String) extends TagValue
  case class IntTag(value: Int) extends TagValue

  type TagMap = Map[String, TagValue]
  object TagMap {
    def empty: TagMap = Map.empty

    /**
      * This is the slightly-ugly result of design decision in circe JSON library not to expose the JSON AST
      * (most other Scala JSON libraries do expose it for pattern-matching)
     */
    def fromJsonObject(rawTags: JsonObject) = {
      rawTags.toMap.foldLeft(TagMap.empty) {
        case (map, (key, jval)) =>

          // with pattern-matching on AST this would be:

          // val tval = jval match {
          //  case JNumber(n) => n.toInt.map(IntTag)
          //  case JString(s) => Some(StringTag(s))
          //  case _          => None
          // }
          //
          // which looks cleaner

          val tval: Option[TagValue] =
            jval.fold(
              None,
              _ => None,
              num => num.toInt.map(IntTag),
              str => Some(StringTag(str)),
              _ => None,
              _ => None)

          tval.fold(map)(v => map + (key -> v))
      }
    }
  }

  /**
    * Ugly hack codec so I don't have to implement custom MsgPack type
    */
  object TagValueMsgPackCodec extends Codec[TagValue] {
    override def pack(out: DataOutputStream, item: TagValue): Unit = item match {
      case StringTag(s) => Format.packString(s"S$s", out)
      case IntTag(i) => Format.packString(s"I$i",out)
    }

    override val unpackFuncMap: FastByteMap[UnpackFunc] =
      RawStringCodecs.StringCodec.unpackFuncMap.mapValues(sup => (in: DataInputStream) => {
        val inStr = sup(in)
        val (test, value) = inStr.splitAt(1)

        test match {
          case "S" => StringTag(value)
          case "I" => IntTag(value.toInt)
        }

      })
  }
}
