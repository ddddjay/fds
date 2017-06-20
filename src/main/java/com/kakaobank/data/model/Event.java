package com.kakaobank.data.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by djyun on 2017. 6. 17..
 */
@Data
public class Event implements Serializable {
    private String eventType;
    private Long eventTime;
    private String accountNo;
    private String customerNo;
    private String targetAccountNo;
    private String targetCustomerNo;
    private String targetBank;
    private Long amount;
}
