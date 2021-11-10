# How to reproduce?

1. Checkout this repo and go to the root folder of the repo
2. `cd docker`
3. `(cd connect-plugins && curl https://d1i4a15mxbxib1.cloudfront.net/api/plugins/confluentinc/kafka-connect-s3/versions/10.0.3/confluentinc-kafka-connect-s3-10.0.3.zip --output confluentinc-kafka-connect-s3-10.0.3.zip && unzip confluentinc-kafka-connect-s3-10.0.3.zip && rm confluentinc-kafka-connect-s3-10.0.3.zip)`
4. `docker-compose up -d`
5. Open a browser and go to `http://localhost:9001/login`. Use admin/password to login and create a readwrite user with access the access key, and secretkey has the secret key. Create a bucket called prototest.
6. `curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/protobuf-to-parquet-test/config --data "@./connector.json"`
7. Check the logs of the connect container to ensure that all is working.
8. Got to the root of the repository and run `./sbt`; once in the repl, execute the command `run` and that will publish 2000 messages to the test topic. You should see and error similar to the following one:
```
org.apache.avro.SchemaParseException: Can't redefine: io.confluent.connect.protobuf.Union.payload
	at org.apache.avro.Schema$Names.put(Schema.java:1511)
	at org.apache.avro.Schema$NamedSchema.writeNameRef(Schema.java:782)
	at org.apache.avro.Schema$RecordSchema.toJson(Schema.java:943)
	at org.apache.avro.Schema$UnionSchema.toJson(Schema.java:1203)
	at org.apache.avro.Schema$RecordSchema.fieldsToJson(Schema.java:971)
	at org.apache.avro.Schema$RecordSchema.toJson(Schema.java:955)
	at org.apache.avro.Schema$UnionSchema.toJson(Schema.java:1203)
	at org.apache.avro.Schema$ArraySchema.toJson(Schema.java:1102)
	at org.apache.avro.Schema$UnionSchema.toJson(Schema.java:1203)
	at org.apache.avro.Schema$RecordSchema.fieldsToJson(Schema.java:971)
	at org.apache.avro.Schema$RecordSchema.toJson(Schema.java:955)
	at org.apache.avro.Schema$RecordSchema.fieldsToJson(Schema.java:971)
	at org.apache.avro.Schema$RecordSchema.toJson(Schema.java:955)
	at org.apache.avro.Schema$RecordSchema.fieldsToJson(Schema.java:971)
	at org.apache.avro.Schema$RecordSchema.toJson(Schema.java:955)
	at org.apache.avro.Schema.toString(Schema.java:396)
	at org.apache.avro.Schema.toString(Schema.java:382)
	at org.apache.parquet.avro.AvroWriteSupport.init(AvroWriteSupport.java:137)
	at org.apache.parquet.hadoop.ParquetWriter.<init>(ParquetWriter.java:277)
	at org.apache.parquet.hadoop.ParquetWriter$Builder.build(ParquetWriter.java:564)
	at io.confluent.connect.s3.format.parquet.ParquetRecordWriterProvider$1.write(ParquetRecordWriterProvider.java:85)
	at io.confluent.connect.s3.format.KeyValueHeaderRecordWriterProvider$1.write(KeyValueHeaderRecordWriterProvider.java:105)
	at io.confluent.connect.s3.TopicPartitionWriter.writeRecord(TopicPartitionWriter.java:532)
	at io.confluent.connect.s3.TopicPartitionWriter.checkRotationOrAppend(TopicPartitionWriter.java:302)
	at io.confluent.connect.s3.TopicPartitionWriter.executeState(TopicPartitionWriter.java:245)
	at io.confluent.connect.s3.TopicPartitionWriter.write(TopicPartitionWriter.java:196)
	at io.confluent.connect.s3.S3SinkTask.put(S3SinkTask.java:234)
	at org.apache.kafka.connect.runtime.WorkerSinkTask.deliverMessages(WorkerSinkTask.java:582)
	at org.apache.kafka.connect.runtime.WorkerSinkTask.poll(WorkerSinkTask.java:330)
	at org.apache.kafka.connect.runtime.WorkerSinkTask.iteration(WorkerSinkTask.java:232)
	at org.apache.kafka.connect.runtime.WorkerSinkTask.execute(WorkerSinkTask.java:201)
```

## What's the problem?

We have multiple `oneOf` with the same name at different levels:
```
syntax = "proto3";

option java_multiple_files = true;
option java_package = "example.schema.event.v1";

package example.event.v1;

message Event {
  string event_id = 1;
  Target target = 2;

  message Target {
    oneof payload {
      string lightbulb_id = 1;
      string switch_id = 2;
    }
  }

  oneof payload {
    AutomatedAction automated = 10;
    ManualAction manual = 11;
  }

  message AutomatedAction {
    oneof payload {
      Action action = 1;
    }
  }

  message ManualAction {
    oneof payload {
      Action action = 1;
    }
  }

  enum Action {
    ON  = 0;
    OFF = 1;
  }

}
```
