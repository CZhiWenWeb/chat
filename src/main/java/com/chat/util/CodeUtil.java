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

	//private static int findBytes(byte[] source, int offset, int count,
	//                             byte[] target, int offsetT, int countT) {
	//	int[] right = new int[256];
	//
	//	int skip;
	//	for (int i = offset; i <= offset + count - countT; i += skip) {
	//		for (int j = offsetT + count - 1; j >= 0; j--) {
	//			skip = 0;
	//			if (source[i + j] != target[j]) {
	//				skip = j -
	//			}
	//			if (skip == 0)
	//				return i;
	//		}
	//	}
	//}

	public static void main(String[] args) throws Exception {
		int i = CodeUtil.binaryStringToInt("001101");
	}
}
