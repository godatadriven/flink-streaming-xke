package com.gdd.hints;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer08;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;

public class StreamingSkeleton {

    public static void main(String[] args) throws Exception {
        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("bootstrap.servers", "localhost:9092");
        kafkaConsumerProps.setProperty("group.id", "test-consumer-group");
        kafkaConsumerProps.setProperty("zookeeper.connect", "localhost:2181");

        // set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Uses the SimpleConsumer API of Kafka internally. Offsets are committed to ZK by Flink.
        DataStream<String> textStream = env
                .addSource(new FlinkKafkaConsumer08<>("yourTopic", new SimpleStringSchema(), kafkaConsumerProps));


        /*
         *
         * then, transform the resulting DataStream<String> using operations
         * like
         * 	.filter()
         * 	.flatMap()
         * 	.print()
         *
         * and many more.
         * Have a look at the programming guide for the Java API:
         *
         * https://ci.apache.org/projects/flink/flink-docs-master/apis/streaming/index.html
         *
         */

        // execute program
        env.execute("Flink Streaming Java API Skeleton");
    }
}
