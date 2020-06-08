package com.chat.cache.util;

import redis.clients.jedis.BinaryJedisCluster;
import redis.clients.jedis.HostAndPort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: czw
 * @CreateTime: 2020-06-08 17:08
 * @UpdeteTime: 2020-06-08 17:08
 * @Description:
 */
public class RedisClientTool implements ICacheTool {
	public String ip = "127.0.0.1";
	public String redisServerPort = "6379";
	private BinaryJedisCluster jedis;

	public RedisClientTool() {
		Set<HostAndPort> jedisClusterNode = new HashSet<>();
		jedisClusterNode.add(new HostAndPort(ip, Integer.parseInt(redisServerPort)));
		jedis = new BinaryJedisCluster(jedisClusterNode, 2000, 5);
	}

	@Override
	public String getString(String key) {
		return null;
	}

	@Override
	public void set(String key, Object value, int month) {
		jedis.set(key,)
	}

	static class SerializeUtil{

		static byte[] serialize(Object obj){
			byte[] bytes=null;
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			try {
				ObjectOutputStream oos=new ObjectOutputStream(bos);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
