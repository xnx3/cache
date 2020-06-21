package com.xnx3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import com.xnx3.cache.JavaUtil;
import com.xnx3.cache.RedisUtil;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 缓存工具。
 * 若使用Redis，在 application.properties 中配置了redis，那么这里是使用redis进行的缓存
 * 如果没有使用redis，那么这里使用的是 Hashmap 进行的缓存
 * @author 管雷鸣
 *
 */
public class CacheUtil {
	public static final int EXPIRETIME = 7*24*60*60;	//7天，默认过期时间
	public static boolean useRedis = false;	//默认不使用redis缓存，而是使用Java 的 map 缓存。 false为不使用redis
	
    /**
     * 初始化Redis连接池
     */
    static {
		System.out.println(CacheUtil.class.getResource("/").getPath());
		String path = CacheUtil.class.getResource("/").getPath();	// classes 根路径
		Properties properties = new Properties();
        try {
        	properties.load(new FileInputStream(path+"application.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        if(!properties.isEmpty()){
        	//配置文件若存在，才会进行下面操作
        	RedisUtil.host = properties.getProperty("spring.redis.host");
        	
            String portStr = properties.getProperty("spring.redis.port");
        	if(portStr != null && portStr.length() > 0){
        		RedisUtil.port = Integer.parseInt(portStr);
        		if(RedisUtil.port == -1){
        			RedisUtil.port = 6379;
        		}
        	}
        	String pwd = properties.getProperty("spring.redis.password");
        	if(pwd != null && pwd.length() > 0){
        		RedisUtil.password = pwd;
        	}
        	String timeoutStr = properties.getProperty("spring.redis.timeout");
        	if(timeoutStr != null && timeoutStr.length() > 0){
        		RedisUtil.timeout = Integer.parseInt(timeoutStr);
        	}
    	}else{
    		RedisUtil.host = null;	//标注为不使用redis
    	}
    	
    	if(RedisUtil.host != null){
    		//只要配置了host，那便认为启用了redis
    		useRedis = true;
    		try {
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(RedisUtil.MAX_ACTIVE);
                config.setMaxIdle(RedisUtil.MAX_IDLE);
                config.setMaxWaitMillis(RedisUtil.MAX_WAIT);
                config.setTestOnBorrow(RedisUtil.TEST_ON_BORROW);
                RedisUtil.jedisPool = new JedisPool(config, RedisUtil.host, RedisUtil.port, RedisUtil.timeout, RedisUtil.password);
                System.out.println(RedisUtil.host+","+RedisUtil.port+","+RedisUtil.timeout);
            } catch (Exception e) {
                e.printStackTrace();
            }
    	}else{
    		//不使用redis
    		useRedis = false;
    	}
    }
	
	/**
	 * 设置缓存
	 * @param key 设置时，多个可以用英文字符:分隔开，就如 user:guanleiming   user:lixin  。同时杜绝一个key对应的value过大的情况！一个value尽可能不要超过10KB
	 * @param value 缓存的值。坚决杜绝value过大，一个value尽可能不要超过10KB，如果太大，建议利用key进行拆分，如 key 为 user.1 存放用户编号为1的缓存信息
	 */
	public static void set(String key, Object value){
		if(useRedis){
			//使用redis
			RedisUtil.setObject(key, value);
		}else{
			//使用 map
			JavaUtil.set(key, value);
		}
	}
	
	/**
	 * 设置缓存
	 * @param key 设置时，多个可以用英文字符:分隔开，就如 user:guanleiming   user:lixin  。同时杜绝一个key对应的value过大的情况！一个value尽可能不要超过10KB
	 * @param value 缓存的值。坚决杜绝value过大，一个value尽可能不要超过10KB，如果太大，建议利用key进行拆分，如 key 为 user.1 存放用户编号为1的缓存信息
	 * @param expiretime 当前key-value的过期时间，单位是秒。比如设定为2，则超过2秒后没使用，会自动删除调。
	 */
	public static void set(String key, Object value, int expiretime){
		if(useRedis){
			//使用redis
			RedisUtil.setObject(key, value, expiretime);
		}else{
			JavaUtil.set(key, value, expiretime);
		}
	}
	
	/**
	 * 设置缓存。该值一周后过期自动删除掉。跟 {@link #set(String, Object)} 不同点，便是这个有一周的倒计时，一周后自动删除掉
	 * @param key 设置时，多个可以用英文字符:分隔开，就如 user:guanleiming   user:lixin  。同时杜绝一个key对应的value过大的情况！一个value尽可能不要超过10KB
	 * @param value 缓存的值。坚决杜绝value过大，一个value尽可能不要超过10KB，如果太大，建议利用key进行拆分，如 key 为 user.1 存放用户编号为1的缓存信息
	 */
	public static void setWeekCache(String key, Object value){
		if(useRedis){
			//使用redis
			RedisUtil.setObject(key, value, EXPIRETIME);
		}else{
			//使用 map
			JavaUtil.set(key, value, -1);
		}
	}
	
	/**
	 * 设置缓存。该值一年后过期自动删除掉
	 * @param key 设置时，多个可以用英文字符:分隔开，就如 user:guanleiming   user:lixin  。同时杜绝一个key对应的value过大的情况！一个value尽可能不要超过10KB
	 * @param value 缓存的值。坚决杜绝value过大，一个value尽可能不要超过10KB，如果太大，建议利用key进行拆分，如 key 为 user.1 存放用户编号为1的缓存信息
	 */
	public static void setYearCache(String key, Object value){
		if(useRedis){
			//使用redis
			RedisUtil.setObject(key, value, 365*24*60*60);
		}else{
			//使用 map
			JavaUtil.set(key, value);
		}
	}
	
	
	/**
	 * 获取缓存信息
	 * @param key 
	 * @return 如果缓存中没有，会返回 null
	 */
	public static Object get(String key){
		if(useRedis){
			//使用redis
			return RedisUtil.getObject(key);
		}else{
			//使用 map
			return JavaUtil.get(key);
		}
	}
	
	/**
	 * 从缓存中，删除某个key
	 * @param key 要删除的缓存的key
	 */
	public static void delete(String key){
		if(useRedis){
			//使用redis
			RedisUtil.delkeyObject(key);
		}else{
			//使用 map
			JavaUtil.delete(key);
		}
	}
	
	 /**
     * 当前是否使用redis
     * @return true:使用redis  false:不使用redis
     */
	public static boolean isUseRedis(){
		return RedisUtil.isUse();
	}
}
