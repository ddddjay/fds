package com.kakaobank.data.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by djyun on 2017. 6. 17..
 */
@Data
public class Account implements Serializable {
    private String accountNo;
    private String customerNo;
    private Long createdTime;
    private Long balance;
}
