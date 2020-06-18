package com.chat.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: czw
 * @CreateTime: 2020-06-18 15:28
 * @UpdeteTime: 2020-06-18 15:28
 * @Description:
 */
public class SingleFactory {
	private SingleFactory() {
	}

	static public Map getSocketMapInstance() {
		return MapHolder.map;
	}

	private static class MapHolder {
		static Map map = new HashMap();
	}
}
