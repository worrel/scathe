package com.worrel.scathe.services.impl

import com.spingo.op_rabbit.{RabbitMarshaller, RabbitUnmarshaller}

object RabbitMsgPackSupport {
  import org.velvia.msgpack._

  implicit def msgpackMarshaller[T: Codec]: RabbitMarshaller[T] = new RabbitMarshaller[T] {
    override def marshall(value: T): Array[Byte] = pack(value)
    override protected def contentType: String = "application/msgpack"
    override protected def contentEncoding: Option[String] = None
  }

  implicit def msgpackUnmarshaller[T: Codec]: RabbitUnmarshaller[T] = new RabbitUnmarshaller[T] {
    override def unmarshall(value: Array[Byte], contentType: Option[String], contentEncoding: Option[String]): T =
      unpack(value)
  }
}
