package com.kakaobank.data.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by djyun on 2017. 6. 18..
 */
@Data
public class EventDetectorGroup implements Serializable{
    String detectorGroupId;
    String detectorGroupName;
    List<String> detectorIdList;
    String serviceYN;
}
