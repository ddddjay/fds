package com.kakaobank.data.dao;


import com.kakaobank.data.BaseClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by djyun on 2017. 6. 19..
 */
public class RedisDaoTest extends BaseClass {
	
	@Autowired
	private RedisDao<String, Object> redisDao;
	
	@Test
	public void getDataTest() {

	}
	
	@Test
	public void incrementTest() {
		long increment = redisDao.increment("test12", "test1", 1);
		
		Integer result = (Integer) redisDao.getHashValue("test12", "test1");
		
		System.out.println(result);
	}

}
