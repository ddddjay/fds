package com.kakaobank.data.service;

import com.kakaobank.data.BaseClass;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by djyun on 2017. 6. 20..
 */
public class ConsumerTest extends BaseClass {
    @Autowired
    private KafkaConsumer kafkaConsumer;

    @Test
    private void consumerTest(){

    }
}
