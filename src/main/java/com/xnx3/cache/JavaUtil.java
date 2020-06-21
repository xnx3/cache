package com.xnx3.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 纯Java实现的 Cache 缓存，可以不依赖任何三方
 * @author 管雷鸣
 *
 */
public class JavaUtil {
	private static Map<String, ValueBean> map;	//当不用redis时，缓存用
	public static final int EXPIRETIME = 7*24*60*60;	//7天，默认过期时间
	static{
		map = new HashMap<String, ValueBean>();
	}
	
	/**
	 * 设置缓存
	 * @param key 设置时，多个可以用英文字符:分隔开，就如 user:guanleiming   user:lixin  。同时杜绝一个key对应的value过大的情况！一个value尽可能不要超过10KB
	 * @param value 缓存的值。坚决杜绝value过大，一个value尽可能不要超过10KB，如果太大，建议利用key进行拆分，如 key 为 user.1 存放用户编号为1的缓存信息
	 * @param expiretime 当前key-value的过期时间，单位是秒。比如设定为2，则超过2秒后没使用，会自动删除。 如果设置为-1，则是永远不会过期。（当然重启tomcat，这里的也就都没了）
	 */
	public static void set(String key, Object value, int expiretime){
		ValueBean valueBean = new ValueBean();
		valueBean.setValue(value);
		if(expiretime > 1){
			valueBean.setExpiretime(getCurrentTime()+expiretime);
		}
		map.put(key, valueBean);
	}
	

	/**
	 * 设置不会过期的缓存（当然重启tomcat，这里的也就都没了）
	 * @param key 设置时，多个可以用英文字符:分隔开，就如 user:guanleiming   user:lixin  。同时杜绝一个key对应的value过大的情况！一个value尽可能不要超过10KB
	 * @param value 缓存的值。坚决杜绝value过大，一个value尽可能不要超过10KB，如果太大，建议利用key进行拆分，如 key 为 user.1 存放用户编号为1的缓存信息
	 */
	public static void set(String key, Object value){
		set(key, value, -1);
	}
	

	/**
	 * 获取缓存信息
	 * @param key 要获取的缓存的key
	 * @return 如果缓存中没有，或者已过期的，都会返回 null
	 */
	public static Object get(String key){
		ValueBean vb = map.get(key);
		if(vb == null){
			//缓存中根本不存在
			return null;
		}
	
		//缓存中存在，那么判断一下它过期时间
		if(vb.getExpiretime() == -1){
			//永不过期，那么直接返回
			return vb.getValue();
		}
		
		//判断是否已过期
		int currentTime = getCurrentTime();
		if(vb.getExpiretime() < currentTime){
			//已过期，那么删除掉，同时返回null
			map.remove(key);
			return null;
		}else{
			//未过期，返回对象
			return vb.getValue();
		}
	}
	
	/**
	 * 从缓存中删除某个key
	 * @param key 要删除的key
	 */
	public static void delete(String key){
		if(key != null){
			map.remove(key);
		}
	}
	
	/**
	 * 获取当前的10位时间戳
	 * @return
	 */
	private static int getCurrentTime(){
		Date date = new Date();
		long time = date.getTime();
		return (int) (time/1000);
	}
}
/**
 * Java模拟redis缓存的过期时间。这里是存入map的value
 * @author 管雷鸣
 */
class ValueBean{
	private Object value;
	private int expiretime;	//过期时间，具体的10位时间戳，超过这个时间戳的，将是过期。 如果为-1则是无过期时间，value不过期
	
	public ValueBean() {
		this.expiretime = -1;
	}
	
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public int getExpiretime() {
		return expiretime;
	}
	/**
	 * 这存储的是具体的10位时间戳，真实过期的未来的某个时间
	 * @param expiretime
	 */
	public void setExpiretime(int expiretime) {
		this.expiretime = expiretime;
	}
}