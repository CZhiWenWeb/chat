package com.chat;

import java.io.Serializable;

/**
 * @Author: czw
 * @CreateTime: 2020-06-09 10:04
 * @UpdeteTime: 2020-06-09 10:04
 * @Description:
 */
public class Person implements Serializable {
	private static final long serialVersionUID = 3019070147903893841L;

	public Person(String name, String pw) {
		this.name = name;
		this.password = pw;
	}

	private String name;
	private String password;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
