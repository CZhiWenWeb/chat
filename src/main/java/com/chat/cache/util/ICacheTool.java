package com.chat.cache.util;


import java.util.Set;

public interface ICacheTool {

	Object getString(String key);

	void set(String key, Object val, int second);

	long setAdd(String key, String... value);

	long setDel(String key, String... value);

	Set<String> getAllValues(String key);
}
