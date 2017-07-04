package com.kakaobank.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaobank.data.model.Event;
import com.kakaobank.data.model.EventDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by djyun on 2017. 6. 13..
 */
@Slf4j
@Service
public class EventConsumerService implements Runnable {
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final List<String> topics = new ArrayList<>();

    @Autowired
    private Environment env;

    @Autowired
    private KafkaConsumer<String, Object> kafkaConsumer;

    @Autowired
    private EventDetectService eventDetect;

    @Autowired
    private AccountService accountService;

    public void run() {
        topics.add(env.getProperty("consume.topic"));
        try {
            kafkaConsumer.subscribe(topics);
            ArrayList<EventDetector> edg = (ArrayList) eventDetect.getDetectorGroup("detectorGroup_1");
            while (true) {
                for (ConsumerRecord<String, Object> record : kafkaConsumer.poll(Long.MAX_VALUE)) {
                    String eventString = record.value().toString();
                    log.info("raw event string : " + eventString);
                    try {
                        //JSON from String to Object
                        ObjectMapper mapper = new ObjectMapper();
                        Event event = mapper.readValue(eventString, Event.class);
                        log.info("Event : " + event);

                        // 계좌 정보 갱신
                        accountService.service(event);

                        // 이벤트 탐지
                        if (!event.getEventType().equals("create"))
                            for (EventDetector detector : edg) {
                                log.info("Detector : " + detector);
                                if (!eventDetect.detect(detector, event)) {
                                    break;
                                }
                                ;
                            }

                    } catch (Exception e) {
                        log.error("Consumer Error : " + e.getMessage());
                    }
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown
            if (!closed.get()) throw e;
        } finally {
            kafkaConsumer.close();
        }
    }

    // Shutdown hook which can be called from a separate thread
    public void shutdown() {
        closed.set(true);
        kafkaConsumer.wakeup();
    }
}

