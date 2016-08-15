package com.gdd.solutions;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer08;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;

import java.util.Properties;

public class KafkaStreamingWordCount {

    public static void main(String[] args) throws Exception {
        ParameterTool tool = ParameterTool.fromArgs(args);

        String topic = tool.getRequired("kafka.topic");

        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("bootstrap.servers", tool.getRequired("kafkabroker"));
        kafkaConsumerProps.setProperty("group.id", tool.getRequired("kafka.groupId"));
        kafkaConsumerProps.setProperty("zookeeper.connect", tool.get("zookeeper.host", "localhost:2181"));
        kafkaConsumerProps.setProperty("auto.offset.reset", tool.getBoolean("from-beginning", false) ? "smallest" : "largest");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> textStream = env
                .addSource(new FlinkKafkaConsumer08<>(topic, new SimpleStringSchema(), kafkaConsumerProps));

        textStream.flatMap(new LineSplitter())
            .keyBy(0)
            .sum(1)
            .print();

        env.execute("WordCount from Kafka Example");
    }

    //
    // 	User Functions
    //

    /**
     * Implements the string tokenizer that splits sentences into words as a user-defined
     * FlatMapFunction. The function takes a line (String) and splits it into
     * multiple pairs in the form of "(word,1)" (Tuple2<String, Integer>).
     */
    public static final class LineSplitter implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // normalize and split the line
            String[] tokens = value.toLowerCase().split("\\W+");

            // emit the pairs
            for (String token : tokens) {
                if (token.length() > 0) {
                    out.collect(new Tuple2<String, Integer>(token, 1));
                }
            }
        }
    }
}
