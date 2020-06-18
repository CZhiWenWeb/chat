package com.chat.cache.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ResourceBundle;

/**
 * @Author: czw
 * @CreateTime: 2020-06-10 16:21
 * @UpdeteTime: 2020-06-10 16:21
 * @Description:
 */
public class JedisUtil {
	private JedisUtil() {
	}

	private static JedisPool jedisPool;
	private static int maxtotal;
	private static int maxwaitmillis;
	private static String host;
	private static int port;

	static {
		//读取配置文件
		ResourceBundle rb = ResourceBundle.getBundle("jedis");
		maxtotal = Integer.parseInt(rb.getString("maxtotal"));
		maxwaitmillis = Integer.parseInt(rb.getString("maxwaitmillis"));
		host = rb.getString("host");
		port = Integer.parseInt(rb.getString("port"));
		//创建连接池
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(maxtotal);
		jedisPoolConfig.setMaxWaitMillis(maxwaitmillis);
		jedisPool = new JedisPool(jedisPoolConfig, host, port);

		//尝试预存放连接
		Jedis[] jedis = new Jedis[3];
		for (int i = 0; i < 3; i++)
			jedis[i] = jedisPool.getResource();
		for (int i = 0; i < 3; i++)
			jedis[i].close();
	}

	public static Jedis getJedis() {
		return jedisPool.getResource();
	}

	public static void main(String[] args) {
		Jedis jedis = JedisUtil.getJedis();
		System.out.println(jedis);
	}
}
