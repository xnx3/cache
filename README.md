# cache
缓存，默认使用java进行缓存，可配置redis缓存，两者可任意来回切换

````
//写
CacheUtil.set(key, value);
CacheUtil.set(key, value, timeout);

//读
CacheUtil.get(Key);
````

# 在 SpringBoot 中的使用
如果要使用 redis，那么需要有 application.properties 文件，其中redis配置：
````
# 是否启用redis，都注释掉为不启用，host不注释则是启用redis
spring.redis.host=127.0.0.1
spring.redis.port=6379
# 如果redis没有设置密码，请把下面的 spring.redis.password 注释掉
#spring.redis.password=pwd
#超时时间，单位是毫秒
#spring.redis.timeout=3000
````

如果不使用redis，只是使用Java Map 进行缓存，那么不需要任何配置。


# 在其他java项目中使用
加入 xnx3-cache-1.0.jar
即可直接使用 Java 本身进行的缓存。

### 使用 redis 缓存
````
//设置redis参数，即可直接使用
RedisUtil.createJedisPool(String host, int port, String password);
CacheUtil.set(key, value);
......
````