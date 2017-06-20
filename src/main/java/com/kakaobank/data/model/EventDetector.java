package com.kakaobank.data.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by djyun on 2017. 6. 17..
 */
@Data
public class EventDetector extends EventDetectorGroup implements Serializable {
    private String detectorId;
    private String index;
    private String detectorType;
    private Long amountUpperLimit;
    private Long amountLowerLimit;
    private Long timeUpperLimit;
    private Long timeLowerLimit;
    private String previousDetectorId;
    private String nextDetectorId;
}
