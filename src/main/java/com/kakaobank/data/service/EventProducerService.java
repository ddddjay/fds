package com.kakaobank.data.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by djyun on 2017. 6. 18..
 */
@Component
public class EventProducerService {

    @Autowired
    private KafkaProducer<String, String> kafkaProducer;

    public void publish(String topic, String msg) {
        try {
            ProducerRecord record = new ProducerRecord<String,String>(topic,msg);
            kafkaProducer.send(record);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            kafkaProducer.close();
        }
    }

    public void flush() {
        kafkaProducer.flush();
    }

    public void close() {
        kafkaProducer.close();
    }
}