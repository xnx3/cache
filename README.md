# cache
缓存，默认使用java进行缓存，可配置redis缓存，两者可任意来回切换

## Maven引入
````
<dependency>
	<groupId>com.xnx3.cache</groupId>
	<artifactId>xnx3-cache</artifactId>
	<version>1.1.1</version>
</dependency>
````

## 代码中使用
#### 写
CacheUtil.set(key, value);  
CacheUtil.set(key, value, timeout);  

#### 读
CacheUtil.get(Key);

## 配置使用redis作为缓存
默认使用的是Java Map 进行缓存，不需要任何配置。如果你想使用redis作为缓存，有以下两种设置方式

#### 方式一，在 SpringBoot 中的使用时，设置配置文件
application.properties 文件中，增加redis配置：
````
# 是否启用redis，都注释掉为不启用，host不注释则是启用redis
spring.redis.host=127.0.0.1
spring.redis.port=6379
# 如果redis没有设置密码，请把下面的 spring.redis.password 注释掉
#spring.redis.password=pwd
#超时时间，单位是毫秒
#spring.redis.timeout=3000
````

#### 方式二，代码设置配置redis
````
//设置redis参数，即可直接使用
RedisUtil.createJedisPool(String host, int port, String password);
......
````