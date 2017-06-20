package com.kakaobank.data.service;

import com.kakaobank.data.dao.RedisDao;
import com.kakaobank.data.model.Account;
import com.kakaobank.data.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by djyun on 2017. 6. 17..
 */
@Slf4j
@Service
public class AccountService {

    @Autowired
    private RedisDao<String, Account> redisDao;

    private static String BANK_ACCOUNT_KEY = "bank::account::";

    public void service(final Event event) {

        switch (event.getEventType()) {
            case "create":
                try {
                    create(event);
                } catch (Exception e) {
                    log.error("신규 계좌정보 추가를 실패하였습니다. : " + e.getMessage());
                }
                break;
            case "deposit":
                try {
                    deposit(event);
                } catch (Exception e) {
                    log.error("출금 정보 갱신이 실패했습니다. : " + e.getMessage());
                }
                break;
            case "withdraw":
                try {
                    withdraw(event);
                } catch (Exception e) {
                    log.error("출금 정보 갱신이 실패했습니다. : " + e.getMessage());
                }
                break;
            case "transfer":
                try {
                    transfer(event);
                } catch (Exception e) {
                    log.error("이체 정보 갱신이 실패했습니다. : " + e.getMessage());
                }
                break;
        }
    }

    private void create(Event event) throws Exception {
        String accountKey = BANK_ACCOUNT_KEY + event.getAccountNo();
        Account account = new Account();
        account.setAccountNo(event.getAccountNo());
        account.setCustomerNo(event.getCustomerNo());
        account.setCreatedTime(event.getEventTime());
        account.setBalance(event.getAmount());

        redisDao.setValue(accountKey, account);
        log.info("신규 계좌정보가 성공적으로 추가되었습니다. : " + account);
    }

    // 입금하기
    private void deposit(Event event) throws Exception {
        String accountKey = BANK_ACCOUNT_KEY + event.getAccountNo();
        Account account = redisDao.getValue(accountKey);

        Long amount = event.getAmount();
        Long currentBalance = account.getBalance();
        Long increaseBalance = currentBalance + amount;
        account.setBalance(increaseBalance);

        redisDao.setValue(accountKey, account);
        log.info("입금 정보가 성공적으로 갱신되었습니다. : " + currentBalance + " -> " + increaseBalance);
    }

    // 출금하기
    private Boolean withdraw(Event event) throws Exception {
        Boolean result;
        String accountKey = BANK_ACCOUNT_KEY + event.getAccountNo();
        Account account = redisDao.getValue(accountKey);

        Long amount = event.getAmount();
        Long currentBalance = account.getBalance();
        Long decreaseBalance = currentBalance - amount;

        try {
            account.setBalance(decreaseBalance);
            redisDao.setValue(accountKey, account);
            result = true;
            log.info("출금 정보가 성공적으로 갱신되었습니다. : " + currentBalance + " -> " + decreaseBalance);
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    //  이체하기
    private void transfer(Event event) throws Exception {
        String accountKey = BANK_ACCOUNT_KEY + event.getAccountNo();
        Account account = redisDao.getValue(accountKey);

        // 내 계좌에서 출금
        Event transferEvent = new Event();
        transferEvent.setEventType("withdraw");
        transferEvent.setAccountNo(event.getAccountNo());
        transferEvent.setCustomerNo(event.getCustomerNo());
        transferEvent.setAmount(event.getAmount());
        transferEvent.setEventTime(event.getEventTime());

        try {
            withdraw(transferEvent);

            transferEvent.setEventType("deposit");
            transferEvent.setAccountNo(event.getTargetAccountNo());
            transferEvent.setCustomerNo(event.getTargetCustomerNo());
            deposit(transferEvent);
            log.info("이체 정보가 성공적으로 갱신되었습니다.");
        } catch (Exception e) {
            // 출금 롤백
            redisDao.setValue(accountKey, account);
            log.error("이체 정보 갱신이 실패하여 롤백했습니다. : " + e.getMessage());
        }
    }
}
