package com.worrel.scathe.services.impl

import akka.actor.{ActorSystem, Props}
import com.worrel.scathe.RabbitQueueConfig
import com.worrel.scathe.models.Messages.{DataEvent, PlatformEvent}
import com.worrel.scathe.models.MsgPack.Codecs._
import com.worrel.scathe.services.{EventConsumer, EventProducer}
import com.worrel.scathe.services.impl.RabbitMsgPackSupport._
import com.spingo.op_rabbit.SubscriptionRef
import com.spingo.op_rabbit.Directives._
import com.spingo.op_rabbit.{Message, RabbitControl, RecoveryStrategy, Subscription}
import com.worrel.scathe.RabbitQueueConfig
import com.worrel.scathe.models.Messages.{DataEvent, PlatformEvent}
import com.worrel.scathe.services.{EventConsumer, EventProducer}



trait RabbitEventService extends EventProducer with EventConsumer {
  self: RabbitQueueConfig =>

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit def actorSystem: ActorSystem
  lazy val rabbitControl = actorSystem.actorOf(Props[RabbitControl])

  def emitEvent(accountId: String, event: PlatformEvent): Unit = {
    implicit val eventCodec = eventMsgPackCodec(event)
    rabbitControl ! Message.exchange(
      event,
      exchange = continuaExchange,
      routingKey = apiEventRoutingKey)
  }

  implicit val recoveryStrategy = RecoveryStrategy.none

  class RabbitConsumerRef(private val ref: SubscriptionRef) extends ConsumerRef {
    override def stop: Unit = ref.close()
  }

  def subscribeToDataEvents(onEvent: DataEvent => Boolean) = {

    val subscriptionRef =
      Subscription.run(rabbitControl) {
        channel(qos = 1000) {
          consume(queue(apiEventsQueue)) {
            body(as[DataEvent]) {
              event =>
                onEvent(event) match {
                  case true => ack()
                  case false => nack()
                }
            }
          }
        }
      }

    new RabbitConsumerRef(subscriptionRef)
  }
}
