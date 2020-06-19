package com.chat.util;

/**
 * @Author: czw
 * @CreateTime: 2020-06-19 08:50
 * @UpdeteTime: 2020-06-19 08:50
 * @Description:
 */
public class CodeUtil {

	public static int binaryStringToInt(String s) throws Exception {
		byte[] chars = s.getBytes();
		int result = 0;
		int len = chars.length;
		for (int i = 0; i < len; i++) {
			if (chars[i] == '1') {
				result = result | (1 << (len - i - 1));
			} else if (chars[i] != 0 && chars[i] != '0') {  //0为空位；'0'字符0；使用byte[]数组生成的string会带上空位字符
				throw new Exception("错误的二进制格式");
			}
		}
		return result;
	}

	public static void main(String[] args) throws Exception {
		int i = CodeUtil.binaryStringToInt("001101");
	}
}
