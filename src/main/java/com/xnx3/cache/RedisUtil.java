package com.xnx3.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;
import com.xnx3.CacheUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis工具类
 * @author 管雷鸣
 *
 */
public class RedisUtil {
    //Redis服务器IP
	public static String host = "127.0.0.1";

    //Redis的端口号
    public static int port = 6379;

    //访问密码
//    private static String AUTH = "pwd";
    public static String password = null;

    //可用连接实例的最大数目，默认值为8；
    //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    public static int MAX_ACTIVE = 1024;

    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    public static int MAX_IDLE = 200;

    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    public static int MAX_WAIT = 10000;

    public static int timeout = 3000;

    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    public static boolean TEST_ON_BORROW = true;

    public static JedisPool jedisPool = null;
    
    /**
     * 初始化Redis连接池
     */
    static {
		String path = RedisUtil.class.getResource("/").getPath();	// classes 根路径
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
    		createJedisPool(RedisUtil.host, RedisUtil.port, RedisUtil.password);
    	}else{
    		//不使用redis
    		System.out.println("com.xnx3.CacheUtil use java map ");
    	}
    }
    
    /**
     * 创建 JedisPool
     * @param host redis的host，如 127.0.0.1
     * @param port redis的port端口，如 6379，必须传入端口号
     * @param password redis的链接密码，如果redis没有密码，要传入 null
     */
    public static void createJedisPool(String host, int port, String password){
    	//如果host、port有值，才会创建
    	if(host != null && host.length() > 0 && port > 0){
    		try {
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(RedisUtil.MAX_ACTIVE);
                config.setMaxIdle(RedisUtil.MAX_IDLE);
                config.setMaxWaitMillis(RedisUtil.MAX_WAIT);
                config.setTestOnBorrow(RedisUtil.TEST_ON_BORROW);
                jedisPool = new JedisPool(config, host, port, RedisUtil.timeout, password);
                System.out.println("com.xnx3.CacheUtil use redis : "+host+","+port+","+RedisUtil.timeout);
                CacheUtil.useRedis = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
    	}
    }
   
    /**
     * 当前是否使用redis
     * @return true:使用redis  false:不使用redis
     */
    public static boolean isUse(){
    	return jedisPool != null;
    }
    
    /**
     * 获取Jedis实例。
     * 注意，用完后要执行 {@link #closeJedis(Jedis)} 关闭
     * @return {@link Jedis}
     */
    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 释放jedis资源
     * @param jedis 要关闭的jredis
     * @deprecated 请使用 {@link #closeJedis(Jedis)}
     */
    public static void returnResource(final Jedis jedis) {
    	closeJedis(jedis);
    }

    /**
     * 关闭jedis
     * @param jedis 要关闭的jredis
     */
    public static void closeJedis(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }
    
    /**
     * 获取redis键值-object
     * 
     * @param key
     * @return
     */
    public static Object getObject(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[] bytes = jedis.get(key.getBytes());
            if(bytes != null && bytes.length > 0) {
                return SerializeUtils.deserialize(bytes);
            }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            jedis.close();
        }
        return null;
    }

    /**
     * 设置redis键值-object
     * @param key
     * @param value
     * @param expiretime
     * @return
     */
    public static String setObject(String key, Object value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.set(key.getBytes(), SerializeUtils.serialize(value));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if(jedis != null)
            {
                jedis.close();
            }
        }
    }
    
    /**
     * 设定key-value
     * @param expiretime 当前key-value的过期时间，单位是秒。比如设定为2，则超过2秒后没使用，会自动删除调
     * @return
     */
    public static String setObject(String key, Object value,int expiretime) {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.set(key.getBytes(), SerializeUtils.serialize(value));
            if(result.equals("OK")) {
                jedis.expire(key.getBytes(), expiretime);
            }
            return result;
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            if(jedis != null)
            {
                jedis.close();
            }
        }
        return result;
    }

    /**
     * 删除key
     */
    public static Long delkeyObject(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.del(key.getBytes());
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }finally{
            if(jedis != null)
            {
                jedis.close();
            }
        }
    }

    public static Boolean existsObject(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(key.getBytes());
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }finally{
            if(jedis != null)
            {
                jedis.close();
            }
        }
    }
    
    
    /**
	 * 获取
	 */
	public static Object deserialize(byte[] bytes) {
		Object result = null;
		if (isEmpty(bytes)) {
			return null;
		}
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);
				try {
					result = objectInputStream.readObject();
				}
				catch (ClassNotFoundException ex) {
					throw new Exception("Failed to deserialize object type", ex);
				}
			}
			catch (Throwable ex) {
				throw new Exception("Failed to deserialize", ex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public static boolean isEmpty(byte[] data) {
		return (data == null || data.length == 0);
	}
	public static byte[] serialize(Object object) {
		byte[] result = null;
		if (object == null) {
			return new byte[0];
		}
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(128);
			try  {
				if (!(object instanceof Serializable)) {
					throw new IllegalArgumentException(SerializeUtils.class.getSimpleName() + " requires a Serializable payload " +
							"but received an object of type [" + object.getClass().getName() + "]");
				}
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
				objectOutputStream.writeObject(object);
				objectOutputStream.flush();
				result =  byteStream.toByteArray();
			}
			catch (Throwable ex) {
				throw new Exception("Failed to serialize", ex);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
    
}
/*
 * 序列化
 */
class SerializeUtils{
	/**
	 * 反序列化
	 */
	public static Object deserialize(byte[] bytes) {
		Object result = null;
		if (isEmpty(bytes)) {
			return null;
		}
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);
				try {
					result = objectInputStream.readObject();
				}
				catch (ClassNotFoundException ex) {
					throw new Exception("Failed to deserialize object type", ex);
				}
			}
			catch (Throwable ex) {
				throw new Exception("Failed to deserialize", ex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public static boolean isEmpty(byte[] data) {
		return (data == null || data.length == 0);
	}
	/**
	 * 序列化
	 */
	public static byte[] serialize(Object object) {
		byte[] result = null;
		if (object == null) {
			return new byte[0];
		}
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(128);
			try  {
				if (!(object instanceof Serializable)) {
					throw new IllegalArgumentException(SerializeUtils.class.getSimpleName() + " requires a Serializable payload " +
							"but received an object of type [" + object.getClass().getName() + "]");
				}
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
				objectOutputStream.writeObject(object);
				objectOutputStream.flush();
				result =  byteStream.toByteArray();
			}
			catch (Throwable ex) {
				throw new Exception("Failed to serialize", ex);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
}