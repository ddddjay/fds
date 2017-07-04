package com.kakaobank.data.service;

import com.kakaobank.data.dao.RedisDao;
import com.kakaobank.data.model.Account;
import com.kakaobank.data.model.Event;
import com.kakaobank.data.model.EventDetector;
import com.kakaobank.data.model.EventDetectorGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by djyun on 2017. 6. 18..
 * <p>
 * FDS 응용로직
 * 1. Detector Group 은 여러 조건의 집합인 하나의 규칙이고, 다수의 Detector(세부조건) 으로 이루어진다.
 * 2. Detector Group 과 Detector 정보는 Redis에 저장. ( 규칙을 등록하는 UI는 차후에 구현 )
 * 3. Event 발생시 Detector Group 을 가져오고, 하위의 Detector가 순차적으로 탐지한다.
 * ㄴ 3.1. 각 Detector 에 명시된 조건( 금액 혹은 시간 상/하한)을 Event에 적용한다.
 * ㄴ 3.2. 각 Detector에 탐지되면 해당 Event는 Detector id 와 AccountNo 를 키로 하여 Redis에 저장한다.
 * ㄴ 3.3. 다음 순서의 Detector 가 시간 조건이 있는 경우, 이전 Detector id에 저장된 이벤트 시간을 가져와서 비교한다.
 * 4. 마지막 Detector 가 통과되면 kafka producer로 Msg를 publishing한다.
 */
@Slf4j
@Service
public class EventDetectService {

    @Autowired
    private Environment env;

    @Autowired
    private RedisDao<String, Object> redisDao;

    @Autowired
    private EventProducerService kafkaProducer;

    private static String BANK_ACCOUNT_KEY = "bank::account::";
    private static String BANK_DETECTOR_GROUP_KEY = "bank::detectorgroup";
    private static String BANK_DETECT_KEY = "bank::detect::";

    // 탐지로직 : 이상 탐지된 이벤트 => false
    public Boolean detect(EventDetector eventDetector, Event event) {
        Boolean isDetect = false;
        // 계좌정보 가져오기
        String accountNo = event.getAccountNo();
        Account account = (Account) redisDao.getValue(BANK_ACCOUNT_KEY + accountNo);

        // 이전 이벤트가 탐지조건에 이미 탐지되었는지 확인
        String detectedEventRedisKey = BANK_DETECT_KEY + eventDetector.getDetectorGroupId() + "::" + eventDetector.getDetectorId() + "::" + event.getAccountNo();
        Boolean isExistDetected = redisDao.isExist(detectedEventRedisKey);
        if (isExistDetected) {
            log.info("Detected : " + eventDetector);
            isDetect = true;
        } else {
            switch (eventDetector.getDetectorType()) {
                case "create":
                    isDetect = isDetectCreatedTime(eventDetector, event, account);
                    break;
                case "balance":
                    isDetect = isDetectBalance(eventDetector, event);
                    break;
                default:
                    isDetect = isDetectAmountAndTime(eventDetector, event);
                    break;
            }
        }

        // 마지막 조건에 탐지되면 Kafka 'detect' Topic 에 메시지 발송
        if (isDetect && eventDetector.getNextDetectorId() == null) {
            String msg = "Detect Fraud Event !!!! , EventDetector ID : " + eventDetector.getDetectorGroupId() + ", Account : " + account + ", Event : " + event;
            log.info("Message :::: " + msg);

            Boolean isSend = sendMsg(msg);
            if (isSend) log.info("Detect Msg publishing Success !!!!!!!!");
            else log.info("Detect Msg publishing Failed !!!!!!!!");
        } else {
            log.info("Message :::: It is NOT Fraud Event");
        }

        return isDetect;
    }

    // 생성시간 조건 탐지
    private Boolean isDetectCreatedTime(EventDetector eventDetector, Event event, Account account) {
        Boolean isDetect = false;
        // 계좌 생성시간
        Long createdTime = account.getCreatedTime();

        // 계좌 생생시간 조건
        isDetect = checkTimeCondition(eventDetector, createdTime);

        // 탐지되면 해당 detectorId key로 Redis에 탐지된 이벤트 저장 & setExpireTime
        if (isDetect) {
            try {
                saveDetectedEvent(eventDetector, event);
            } catch (Exception e) {
                log.error("Failed Save detected event ! : " + eventDetector + " & " + event + " => " + e.getMessage());
            }
        }
        return isDetect;
    }

    // 잔액 조건 탐지
    private Boolean isDetectBalance(EventDetector eventDetector, Event event) {
        Boolean isDetect = false;
        // 계좌 잔액 정보
        Account account = (Account) redisDao.getValue(BANK_ACCOUNT_KEY + event.getAccountNo());
        // 이전 조건에서 탐지된 이벤트 발생 시간 가져오기
        String detectedEventRedisKey = BANK_DETECT_KEY + eventDetector.getDetectorGroupId() + "::" + eventDetector.getPreviousDetectorId() + "::" + event.getAccountNo();
        Event previousDetectedEvent = (Event) redisDao.getValue(detectedEventRedisKey);
        // 잔액 조건
        if (isExistAmountClause(eventDetector) && isExistTimeClause(eventDetector)) {
            isDetect = checkAmountCondition(eventDetector, account.getBalance()) && checkTimeCondition(eventDetector, previousDetectedEvent.getEventTime());
        } else if (isExistAmountClause(eventDetector)) {
            isDetect = checkAmountCondition(eventDetector, account.getBalance());
        } else if (isExistTimeClause(eventDetector)) {
            isDetect = checkTimeCondition(eventDetector, previousDetectedEvent.getEventTime());
        }
        // 탐지되면 해당 detectorId key로 Redis에 탐지된 이벤트 저장 & setExpireTime
        if (isDetect) {
            try {
                saveDetectedEvent(eventDetector, event);
            } catch (Exception e) {
                log.error("Failed Save detected event ! : " + eventDetector + " & " + event + " => " + e.getMessage());
            }
        }
        return isDetect;
    }

    // 금액 or 시간 조건 탐지
    private Boolean isDetectAmountAndTime(EventDetector eventDetector, Event event) {
        Boolean isDetect = false;
        // 이전 조건에서 탐지된 이벤트 발생 시간 가져오기
        String detectedEventRedisKey = BANK_DETECT_KEY + eventDetector.getDetectorGroupId() + "::" + eventDetector.getPreviousDetectorId() + "::" + event.getAccountNo();
        Event previousDetectedEvent = (Event) redisDao.getValue(detectedEventRedisKey);
        // 시간 && 금액 조건
        Boolean isTypeEqual = eventDetector.getDetectorType().equals(event.getEventType());
        if (isTypeEqual) {
            if (isExistAmountClause(eventDetector) && isExistTimeClause(eventDetector)) {
                isDetect = checkAmountCondition(eventDetector, event.getAmount())
                        && checkTimeCondition(eventDetector, previousDetectedEvent.getEventTime());
            } else if (isExistAmountClause(eventDetector)) {
                isDetect = checkAmountCondition(eventDetector, event.getAmount());
            } else if (isExistTimeClause(eventDetector)) {
                isDetect = checkTimeCondition(eventDetector, previousDetectedEvent.getEventTime());
            }
        }
        // 탐지되면 해당 detectorId key로 Redis에 탐지된 이벤트 저장 & setExpireTime
        if (isDetect) {
            try {
                saveDetectedEvent(eventDetector, event);
            } catch (Exception e) {
                log.error("Failed Save detected event ! : " + eventDetector + " & " + event + " => " + e.getMessage());
            }
        }
        return isDetect;
    }

    // 금액 조건 유무 체크
    private Boolean isExistAmountClause(EventDetector eventDetector) {
        if (eventDetector.getAmountUpperLimit() == null && eventDetector.getAmountLowerLimit() == null) {
            return false;
        } else {
            return true;
        }
    }

    // 시간 조건 유무 체크
    private Boolean isExistTimeClause(EventDetector eventDetector) {
        if (eventDetector.getTimeUpperLimit() == null && eventDetector.getTimeLowerLimit() == null) {
            return false;
        } else {
            return true;
        }
    }

    // 상한/하한 조건 체크 : 조건에 벗어나면 false
    private Boolean checkLimit(Long target, Long upperLimit, Long lowerLimit) {
        if (lowerLimit == null && upperLimit == null) {
            return true;
        } else if (lowerLimit == null) {
            return target <= upperLimit;
        } else if (upperLimit == null) {
            return lowerLimit <= target;
        } else {
            return lowerLimit <= target && target <= upperLimit;
        }
    }

    // 시간차이 조건 : 조건에 벗어나면 false
    private Boolean checkTimeCondition(EventDetector eventDetector, Long eventTime) {
        Long diff = System.currentTimeMillis() / 1000 - eventTime;
        Long timeUpperLimit = eventDetector.getTimeUpperLimit();
        Long timeLowerLimit = eventDetector.getTimeLowerLimit();
        return checkLimit(diff, timeUpperLimit, timeLowerLimit);
    }

    // 금액차이 조건 : 조건에 벗어나면 false
    private Boolean checkAmountCondition(EventDetector eventDetector, Long amount) {
        Long amountUpperLimit = eventDetector.getAmountUpperLimit();
        Long amountLowerLimit = eventDetector.getAmountLowerLimit();
        return checkLimit(amount, amountUpperLimit, amountLowerLimit);
    }

    // kafka msg 발송
    private Boolean sendMsg(String msg) {
        Boolean send;
        String topic = env.getProperty("produce.topic");

        try {
            kafkaProducer.publish(topic, msg);
            kafkaProducer.flush();
            send = true;
        } catch (Exception e) {
            send = false;
        } finally {
            kafkaProducer.close();
        }
        return send;
    }

    // 조건별 탐지 결과 저장
    private void saveDetectedEvent(EventDetector eventDetector, Event event) throws Exception {
        String detectedEventRedisKey = BANK_DETECT_KEY + eventDetector.getDetectorGroupId() + "::" + eventDetector.getDetectorId() + "::" + event.getAccountNo();
        redisDao.setValue(detectedEventRedisKey, event);
        // 시간 조건이 있는 경우, 해당 시간으로 expire를 설정. 없는 경우는 디폴트로 1일 보관
        if (eventDetector.getTimeUpperLimit() != null) {
            redisDao.expire(detectedEventRedisKey, eventDetector.getAmountUpperLimit().intValue(), TimeUnit.MILLISECONDS);
        } else {
            redisDao.expire(detectedEventRedisKey, 1000 * 60 * 60 * 24, TimeUnit.MILLISECONDS);
        }
    }

    // 탐지 그룹 전체 가져오기( live 중인 것 )
    public List<EventDetectorGroup> getAllDetectorGroup() {
        List<EventDetectorGroup> result = new ArrayList<>();
        try {
            Long size = redisDao.size(BANK_DETECTOR_GROUP_KEY);
            for (Object obj : redisDao.getRangeList(BANK_DETECTOR_GROUP_KEY, 0, size)) {
                EventDetectorGroup eventDetectorGroup = (EventDetectorGroup) obj;
                if (eventDetectorGroup.getServiceYN().equals("Y")) result.add(eventDetectorGroup);
            }
        } catch (Exception e) {
            log.error("탑지그룹 전체 리스트 로딩 실패 : " + e.getMessage());
        }
        return result;
    }

    // 탐지 그룹 가져오기
    public List<EventDetector> getDetectorGroup(String detectorGroupId) {
        List<EventDetector> eventDetectors = new ArrayList<>();
        try {
            String key = BANK_DETECTOR_GROUP_KEY + "::" + detectorGroupId + "::detectors";
            Long size = redisDao.size(key);
            eventDetectors = (ArrayList) redisDao.getRangeList(key, 0, size);
            log.info("탐지그룹리스트 로딩에 성공했습니다." + eventDetectors);
        } catch (Exception e) {
            log.error("탑지그룹리스트 로딩 실패했습니다. : " + e.getMessage());
        }
        return eventDetectors;
    }

    // 탐지 그룹 생성하기
    public Boolean addDetectorGroup(EventDetectorGroup eventDetectorGroup) {
        Boolean success;
        String detectorGroupId = eventDetectorGroup.getDetectorGroupId();
        try {
            redisDao.rightPush(BANK_DETECTOR_GROUP_KEY, eventDetectorGroup);
            redisDao.setValue(BANK_DETECTOR_GROUP_KEY + "::" + detectorGroupId, eventDetectorGroup);
            log.info("탐지그룹 등록에 성공했습니다. : " + eventDetectorGroup);
            success = true;
        } catch (Exception e) {
            log.error("탐지그룹 등록에 실패하였습니다. : " + e.getMessage());
            success = false;
        }
        return success;
    }

    // 탐지조건을 그룹에 등록하기
    public Boolean addDetector(EventDetector eventDetector) {
        Boolean success;
        String detectorGroupId = eventDetector.getDetectorGroupId();
        String key = BANK_DETECTOR_GROUP_KEY + "::" + detectorGroupId + "::detectors";
        try {
            redisDao.rightPush(key, eventDetector);
            log.info("탐지조건 등록에 성공했습니다. : " + eventDetector);
            success = true;
        } catch (Exception e) {
            log.error("탐지조건 등록에 실패하였습니다. : " + e.getMessage());
            success = false;
        }
        return success;
    }
}
