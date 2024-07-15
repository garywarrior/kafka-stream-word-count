package com.example.kafkawordcount;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Properties;

@SpringBootApplication
public class KafkaWordCountApplication {

    public static void main(String[] args) {

        // SpringApplication.run(KafkaWordCountApplication.class, args);

        Properties prop = new Properties();
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-word-count");
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        prop.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "1000");
        prop.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);


        StreamsBuilder builder = new StreamsBuilder();

        builder.<String, String>stream("sentences").flatMapValues((key, value) -> Arrays.asList(value.toLowerCase().split(" ")))
                .groupBy((key, value) -> value)
                .count(Materialized.with(Serdes.String(), Serdes.Long())).toStream().to("word-count", Produced.with(Serdes.String(),Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), prop);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}
