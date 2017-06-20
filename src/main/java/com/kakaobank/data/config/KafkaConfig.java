package com.kakaobank.data.config;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Properties;

/**
 * Created by djyun on 2017. 6. 18..
 */
@Configuration
public class KafkaConfig {

    @Autowired
    private Environment env;

    @Bean
    @Qualifier("kafkaConsumer")
    public <K, V> KafkaConsumer<K,V> kafkaConsumer(){
        KafkaConsumer<K,V> kafkaConsumer = new KafkaConsumer<>(kafkaProperty());
        return kafkaConsumer;
    }

    @Bean
    @Qualifier("kafkaProducer")
    public <K, V> KafkaProducer<K,V> kafkaProducer(){
        KafkaProducer<K,V> kafkaProducer = new KafkaProducer<>(kafkaProperty());
        return kafkaProducer;
    }

    private Properties kafkaProperty(){
        Properties props = new Properties();
        props.put("bootstrap.servers", env.getProperty("bootstrap.servers"));
        props.put("group.id", env.getProperty("group.id"));
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        return props;
    }
}
