package com.kakaobank.data.service;


import com.kakaobank.data.BaseClass;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by djyun on 2017. 6. 19..
 */
public class ProducerTest extends BaseClass {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private EventProducerService kafkaProducerService;

    @Test
    public void producerTest(){
        kafkaProducerService.publish("detect","test");
//        publish("detect","test");
    }

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

}
