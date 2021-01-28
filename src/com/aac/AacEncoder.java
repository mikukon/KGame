package com.aac;

public class AacEncoder {
	private static AacEncoder m_instance = null;

	public AacEncoder() {

	}

	public static AacEncoder getInstance() {
		if (m_instance == null) {
			m_instance = new AacEncoder();
		}
		return m_instance;
	}

	/**
	 * 初始化
	 * 
	 * @param sampleRate
	 *            采样率
	 * @param channels
	 *            声道数
	 * @param bitsPerSamples
	 *            位数
	 * @return 对象指针
	 */
	public static native long encodeInit(int sampleRate, int channels,
			int bitsPerSamples);

	/**
	 * aac编码
	 * 
	 * @param speechin
	 *            输入语音
	 * @param insize
	 *            输入语音字节数大小
	 * @param out
	 *            编码后输出
	 * @param aac
	 *            aac指针
	 * @return 编码输出字节长度
	 */
	public static native int aacEncode(byte speechin[], int insize, byte out[],
			long aac);

	/**
	 * 释放
	 * 
	 * @param aac
	 *            aac指针
	 * @return 结果 1 成功 ；-1 失败
	 */
	public static native int encodeExit(long aac);

	/**
	 * 获取每次输入sample的数量
	 * 
	 * @param aac
	 *            指针
	 * @return 返回输入sample数量
	 */
	public static native int getInputSample(long aac);

	/**
	 * 获取最大输出字节数
	 * 
	 * @param aac
	 *            aac指针
	 * @return 返回最大编码输出字节数
	 */
	public static native int getMaxOutputBytes(long aac);

	static {
		System.loadLibrary("faac");

	}
}