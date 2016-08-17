package com.gdd.solutions;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.util.Collector;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.flink.core.fs.FileSystem.WriteMode.OVERWRITE;

/**
 * This implementation just counts HashTags within a sliding window. This is the simplest form of trending...
 *
 * Flink has also an example: https://github.com/apache/flink/blob/master/flink-examples/flink-examples-streaming/src/main/java/org/apache/flink/streaming/examples/twitter/TwitterExample.java
 */
public class TwitterStreamProcessing {

    protected static final String JSON_WITH_HASHTAGS = "\"hashtags\":[{\"";
    protected static final Pattern HASHTAG_PATTERN = Pattern.compile("\\{\"text\":\"(\\w+)\"");


    public static void main(String args[]) throws Exception {
        ParameterTool tool = ParameterTool.fromArgs(args);

        Properties p = new Properties();
        p.setProperty(TwitterSource.CONSUMER_KEY, tool.getRequired("consumer.key"));
        p.setProperty(TwitterSource.CONSUMER_SECRET, tool.getRequired("consumer.secret"));
        p.setProperty(TwitterSource.TOKEN, tool.getRequired("token"));
        p.setProperty(TwitterSource.TOKEN_SECRET, tool.getRequired("token.secret"));

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);

        DataStream<String> streamSource = env.addSource(new TwitterSource(p));

        // Every 10 minutes, most trending of last 1 hour of tweets.
        SlidingEventTimeWindows windowSpec = SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(10));

        streamSource.flatMap(hashTagExtraction())
                .keyBy(0)
                .sum(1)
                .windowAll(windowSpec)
                .maxBy(1)
                .writeAsText("file:///Users/abij/projects/tryouts/flink-streaming-xke/hot-hashtags.log", OVERWRITE);

        env.execute("Twitter Stream");
    }

    /**
     * Transform a Tweet into [hashtag, 1] tuples.
     */
    private static FlatMapFunction<String, Tuple2<String, Integer>> hashTagExtraction() {
        return new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String twitterMessage, Collector<Tuple2<String, Integer>> out) throws Exception {
                if (twitterMessage.contains(JSON_WITH_HASHTAGS)) {
                    String hashTagsJson = substringHashtagJson(twitterMessage);
                    collectHashtags(out, hashTagsJson);
                }
            }
        };
    }

    protected static void collectHashtags(Collector<Tuple2<String, Integer>> out, String hashTagsJson) {
        Matcher matcher = HASHTAG_PATTERN.matcher(hashTagsJson);
        while (matcher.find()) {
            String hashTag = matcher.group(1);
            out.collect(new Tuple2<>(hashTag, 1));
        }
    }

    protected static String substringHashtagJson(String twitterMessage) {
        int start = twitterMessage.indexOf("\"hashtags\":[{\"");
        String rest = twitterMessage.substring(start);
        int end = rest.indexOf("],");
        return rest.substring(0, end + 1);
    }
}
