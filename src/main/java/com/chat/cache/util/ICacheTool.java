package com.chat.cache.util;

public interface ICacheTool {

	String getString(String key);

	void set(String key, Object value, int month);
}
