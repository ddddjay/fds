package com.kakaobank.data.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

/**
 * Created by djyun on 2017. 6. 18..
 */
@Slf4j
@Service
public class EventConsumerProcess {

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private EventConsumerService kafkaConsumer;

    public void run() {
        log.info("EventConsumer & EventDetector started !");

        try {

            Thread thread = new Thread(kafkaConsumer);
//            Executors.newFixedThreadPool(5);
            executorService.execute(thread);
            executorService.shutdown();

        } catch (Exception e) {
            log.error("Process Run ERROR : {} ", e.getMessage());
        }
    }

}
