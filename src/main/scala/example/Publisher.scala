package example

import example.schema.event.v1.events_v1.Event
import example.schema.event.v1.{Event => JavaEvent}
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.jdk.CollectionConverters._
import scala.util.Random

object Publisher extends App {

  val events = generator(2000).map(Event.toJavaProto).map { data =>
    new ProducerRecord[String, JavaEvent]("protobuf-topic", data)
  }

  val config: Map[String, AnyRef] = Map(
    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer],
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> classOf[
      KafkaProtobufSerializer[JavaEvent]
    ],
    AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> "http://localhost:8081"
  )

  val producer =
    new KafkaProducer[String, JavaEvent](config.asJava)

  events.foreach(producer.send)
  producer.flush()
  producer.close()

  private def generator(number: Int): List[Event] =
    (0 until number).to(List).map { idx =>
      val target = List(
        Event.Target.Payload.LightbulbId(s"${idx}789"),
        Event.Target.Payload.SwitchId(s"${idx}789")
      )
      val action = List(
        Event.Action.ON,
        Event.Action.OFF
      )
      val payload = List(
        Event.Payload.Automated(
          Event.AutomatedAction(
            Event.AutomatedAction.Payload.Action(Random.shuffle(action).head)
          )
        ),
        Event.Payload.Manual(
          Event.ManualAction(
            Event.ManualAction.Payload.Action(Random.shuffle(action).head)
          )
        )
      )
      Event(
        s"${idx}123",
        Some(
          Event
            .Target(Random.shuffle(target).head)
        ),
        Random.shuffle(payload).head
      )
    }
}
