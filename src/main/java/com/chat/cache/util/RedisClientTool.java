package com.chat.cache.util;

import redis.clients.jedis.Jedis;

import java.io.*;

/**
 * @Author: czw
 * @CreateTime: 2020-06-08 17:08
 * @UpdeteTime: 2020-06-08 17:08
 * @Description:
 */
public class RedisClientTool implements ICacheTool {
	private String ip = "127.0.0.1";
	private int port = 6379;
	private Jedis jedis;
	private SerializeUtil su;
	public RedisClientTool() {
		jedis = new Jedis(ip, port);
		su = new SerializeUtil();
	}

	@Override
	public Object getString(String key) {
		return su.unSerialize(jedis.get(key.getBytes()));
	}

	@Override
	public void set(String key, Object val, int second) {
		if (second == -1)
			jedis.set(key.getBytes(), su.serialize(val));
			//nx不存在才set，xx存在才set;ex是秒，px是毫秒
		else
			jedis.set(key.getBytes(), su.serialize(val), "NX".getBytes(), "EX".getBytes(), second);
	}

	class SerializeUtil {
		//序列化
		byte[] serialize(Object obj) {
			byte[] bytes = null;
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			try {
				ObjectOutputStream oos=new ObjectOutputStream(bos);
				oos.writeObject(obj);
				bytes = bos.toByteArray();
				oos.flush();
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bytes;
		}

		//反序列化
		Object unSerialize(byte[] bytes) {
			Object obj = null;
			try {
				if (bytes != null) {
					ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
					ObjectInputStream ois;
					ois = new ObjectInputStream(bis);
					obj = ois.readObject();
					ois.close();
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			return obj;
		}
	}
}
