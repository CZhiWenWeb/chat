package com.chat.socket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: czw
 * @CreateTime: 2020-06-16 11:31
 * @UpdeteTime: 2020-06-16 11:31
 * @Description:
 */
public class ServerTest {
	public static void main(String[] args) throws IOException {
		int a=1;
		s(a=3+a);
		Server server = new Server(12346);
		server.run();
	}

	public static void s(int i){
		System.out.println(i);
	}
}

class A {
	static Map<Integer, Integer> map = new HashMap<>();
}

class B {
	static Map<Integer, Integer> bm = A.map;
}