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
	private Jedis jedis;
	private SerializeUtil su;
	public RedisClientTool() {
		jedis = JedisUtil.getJedis();
		su = new SerializeUtil();
	}

	@Override
	public Object getString(String key) {
		Object o = su.unSerialize(jedis.get(key.getBytes()));
		//返回连接池
		//jedis.close();
		return o;
	}

	@Override
	public void set(String key, Object val, int second) {
		//if (second == -1)
			jedis.set(key.getBytes(), su.serialize(val));
		//jedis.close();
			//nx不存在才set，xx存在才set;ex是秒，px是毫秒
		//else
		//	jedis.set(key.getBytes(), su.serialize(val));
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
