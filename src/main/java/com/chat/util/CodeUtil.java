package com.chat.util;

import java.nio.charset.StandardCharsets;

/**
 * @Author: czw
 * @CreateTime: 2020-06-19 08:50
 * @UpdeteTime: 2020-06-19 08:50
 * @Description:
 */
public class CodeUtil {

	public static int bytesToInt(byte[] bytes, int offset, int count) throws Exception {
		int result = 0;
		for (int i = 0; i < count; i++) {
			if (bytes[i + offset] == '1') {
				result = result | (1 << (count - i - 1));
			} else if (bytes[i + offset] != 0 && bytes[i + offset] != '0') {  //0为空位；'0'字符0；byte[]默认填充空位字符
				throw new Exception("错误的二进制格式");
			}
		}
		return result;
	}

	/**
	 * @param source  原byte[]
	 * @param offset  开始查询的位置
	 * @param len     需要查询的位数
	 * @param target  目标byte[]
	 * @param offsetT 开始查询位置
	 * @param countT  需要查询的位数
	 * @return 返回第一个符合的下标，不存在返回-1
	 * @throws Exception
	 */
	public static int findBytesByBM(byte[] source, int offset, int len,
	                                byte[] target, int offsetT, int countT) throws Exception {
		if (countT == 0)
			throw new Exception("目标长度不能为0");

		int[] right = new int[256];

		for (int i = offsetT; i < target.length; i++)
			right[target[i]] = i + 1;       //right[index]-1为index在target中的最右下标

		int skip;
		for (int i = offset; i <= offset + len - countT; i += skip) {
			skip = 0;
			for (int j = offsetT + countT - 1; j >= offsetT; j--) {
				if (source[i + j - offsetT] != target[j]) {
					int index = source[i + j - offsetT];   //ascii值
					if (index < 0) {
						skip = (j - offset) + 1;
					} else {
						skip = (j - offsetT) - (right[index] - 1);
					}
					if (skip < 1)
						skip = 1;
					break;
				}
			}
			if (skip == 0)
				return i;
		}
		return -1;
	}

	public static void main(String[] args) throws Exception {
		int i = CodeUtil.bytesToInt("001101".getBytes(), 0, "001101".getBytes().length);
		byte[] b = "from:".getBytes();
		byte[] a = data.getBytes(StandardCharsets.UTF_8);
		int index = 0;
		int start = 0;
		int len = a.length;
		byte[] bytes;
		while (index != -1) {
			index = findBytesByBM(a, start, len,
					b, 0, b.length);
			//System.out.println((char) a[index]);
			//bytes = new byte[index + IdFactory.IDLEN + b.length];
			//int len = bytes.length > a.length - start ? a.length - start : bytes.length;
			//System.arraycopy(a, start, bytes, 0, len);
			//System.out.println(new String(bytes));
			System.out.println("index:" + index);
			start = index + IdFactory.IDLEN + b.length;
			len = a.length - start;
			System.out.println("start:" + start);
		}
	}

	private static String data = "好好学习tiantianxiangshang from:25484893409888好好学习tiantianxiangshang from:25484893401248\n" +
			"好好学习tiantianxiangshang from:25484893409888好好学习tiantianxiangshang from:25484893401248\n" +
			"好好学习tiantianxiangshang from:25484893409888好好学习tiantianxiangshang from:25484893401248\n";
}
