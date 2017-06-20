package com.kakaobank.data.service;


import com.kakaobank.data.BaseClass;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by djyun on 2017. 6. 19..
 */
public class ProducerTest extends BaseClass {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void producerInputTest() {

    }
}
