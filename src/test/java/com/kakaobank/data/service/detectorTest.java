package com.kakaobank.data.service;

import com.kakaobank.data.BaseClass;
import com.kakaobank.data.dao.RedisDao;
import com.kakaobank.data.model.EventDetector;
import com.kakaobank.data.model.EventDetectorGroup;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by djyun on 2017. 6. 20..
 */
public class detectorTest extends BaseClass {
    @Autowired
    private EventDetectService eventDetectService;

    @Autowired
    private RedisDao<String, Object> redisDao;

    @Test
    public void addDetectorGroupTest(){
        String bankDetectorGroupKey = "bank::detectorgroup";

        String eventDetectorGroupId = "detectorGroup_1";
        EventDetectorGroup edg = new EventDetectorGroup();
        edg.setDetectorGroupId(eventDetectorGroupId);
        edg.setDetectorGroupName("Rule-A");
        edg.setServiceYN("Y");

        List detectorIdList = new ArrayList<String>();
        detectorIdList.add(eventDetectorGroupId+".1");
        detectorIdList.add(eventDetectorGroupId+".2");
        detectorIdList.add(eventDetectorGroupId+".3");
        edg.setDetectorIdList(detectorIdList);

//        eventDetectService.addDetectorGroup(edg);

//        redisDao.setValue(bankDetectorGroupKey+"::"+eventDetectorGroupId, edg);
//        redisDao.rightPush(bankDetectorGroupKey,edg);

//        // detector
        EventDetector ed = new EventDetector();
        ed.setDetectorGroupId(eventDetectorGroupId);
        ed.setDetectorId(eventDetectorGroupId+".1");
        ed.setDetectorType("create");
        ed.setTimeUpperLimit(1000 * 60 * 60 * 24 * 7L);
        ed.setNextDetectorId(eventDetectorGroupId+".2");
        ed.setIndex("1");
//        eventDetectService.addDetector(ed);

        EventDetector ed1 = new EventDetector();
        ed1.setDetectorGroupId(eventDetectorGroupId);
        ed1.setDetectorId(eventDetectorGroupId+".2");
        ed1.setDetectorType("deposit");
        ed1.setAmountUpperLimit(1000000L);
        ed1.setAmountLowerLimit(900000L);
        ed1.setIndex("2");
        ed1.setPreviousDetectorId(eventDetectorGroupId+".1");
        ed1.setNextDetectorId(eventDetectorGroupId+".3");
//        eventDetectService.addDetector(ed1);

        EventDetector ed2 = new EventDetector();
        ed2.setDetectorGroupId(eventDetectorGroupId);
        ed2.setDetectorId(eventDetectorGroupId+".3");
        ed2.setDetectorType("balance");
        ed2.setTimeUpperLimit(1000 * 60 * 60 * 2L);
        ed2.setAmountUpperLimit(10000L);
        ed2.setIndex("3");
        ed2.setPreviousDetectorId(eventDetectorGroupId+".2");
//        eventDetectService.addDetector(ed2);

//        eventDetectService.getAllDetectorGroup();
//        eventDetectService.getDetectorGroup(eventDetectorGroupId);

    }
}
