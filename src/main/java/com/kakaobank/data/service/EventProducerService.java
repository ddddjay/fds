package com.kakaobank.data.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by djyun on 2017. 6. 18..
 */
@Service
public class EventProducerService {

    @Autowired
    private KafkaProducer<String, String> kafkaProducer;

    public Boolean publish(String topic, String msg) {
        Boolean isSend = false;
        try {
            ProducerRecord record = new ProducerRecord<String,String>(topic,msg);
            kafkaProducer.send(record);
            isSend = true;
        } catch (Exception e) {
            e.printStackTrace();
        }// finally {
//            kafkaProducer.flush();
//            kafkaProducer.close();
//        }
        return isSend;
    }

    public void flush() {
        kafkaProducer.flush();
    }

    public void close() {
        kafkaProducer.close();
    }
}