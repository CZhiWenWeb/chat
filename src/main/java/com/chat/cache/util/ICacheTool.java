package com.chat.cache.util;


public interface ICacheTool {

	Object getString(String key);

	void set(String key, Object val, int second);
}
