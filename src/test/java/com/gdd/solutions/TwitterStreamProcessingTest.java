package com.gdd.solutions;

import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TwitterStreamProcessingTest {

    @Test
    public void substringHashtag() throws Exception {
        String hashtags = TwitterStreamProcessing.substringHashtagJson("{bla bla bla}, \"hashtags\":[{\"text\":\"NadoSincronizado\",\"indices\":[0,17]},{\"text\":\"MEX\",\"indices\":[18,22]}], {blablabla}");
        assertEquals("\"hashtags\":[{\"text\":\"NadoSincronizado\",\"indices\":[0,17]},{\"text\":\"MEX\",\"indices\":[18,22]}]", hashtags);
    }

    @Test
    public void testCollectHashTags() throws Exception {
        ArrayList<Tuple2<String, Integer>> list = new ArrayList<>();
        Collector<Tuple2<String, Integer>> collector = new ListCollector<>(list);
        TwitterStreamProcessing.collectHashtags(collector, "\"hashtags\":[{\"text\":\"NadoSincronizado\",\"indices\":[0,17]},{\"text\":\"MEX\",\"indices\":[18,22]}]");

        assertThat(list, hasItem(Tuple2.of("NadoSincronizado", 1)));
        assertThat(list, hasItem(Tuple2.of("MEX", 1)));
    }



}