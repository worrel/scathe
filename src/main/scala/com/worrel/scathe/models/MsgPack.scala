package com.worrel.scathe.models

import com.worrel.scathe.models.DataTags.TagValue
import com.worrel.scathe.models.Messages.{DataEvent, DataEventHeader, PlatformEvent}
import org.velvia.msgpack.CaseClassCodecs.{CaseClassCodec2, CaseClassCodec4}
import org.velvia.msgpack.Codec

object MsgPack {

  object Codecs {

    import org.velvia.msgpack.SimpleCodecs._
    import org.velvia.msgpack.CollectionCodecs._
    import org.velvia.msgpack.RawStringCodecs._

    implicit val tagValueCodec = DataTags.TagValueMsgPackCodec
    implicit val tagMapCodec = new MapCodec[String, TagValue]

    implicit val dataEventHeaderCodec: Codec[DataEventHeader] = new CaseClassCodec2(DataEventHeader.apply, DataEventHeader.unapply)
    implicit val dataEventCodec: Codec[DataEvent] = new CaseClassCodec4(DataEvent.apply, DataEvent.unapply)

    def eventMsgPackCodec[A <: PlatformEvent](a: A): Codec[A] = a match {
      case _: DataEvent => dataEventCodec.asInstanceOf[Codec[A]]
    }
  }
}
