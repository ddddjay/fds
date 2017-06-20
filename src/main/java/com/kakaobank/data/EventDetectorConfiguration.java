package com.kakaobank.data;

import com.kakaobank.data.config.JedisConfig;
import com.kakaobank.data.config.KafkaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by djyun on 2017. 6. 18.
 */
@Configuration
@Import({JedisConfig.class, KafkaConfig.class})
@PropertySource({"classpath:/dist-data.properties", "classpath:/database.properties", "classpath:/kafka.properties"})
@ComponentScan(basePackages = "com.kakaobank.data", excludeFilters = @ComponentScan.Filter(Configuration.class))
public class EventDetectorConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public ExecutorService executorService() {

        return Executors.newFixedThreadPool(Integer.parseInt(env.getProperty("thread.pool.size")));

    }


}
