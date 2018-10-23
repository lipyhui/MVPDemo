package com.kawakp.kp.oxygenerator.widget.plc.kawa;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 创建人: penghui.li
 * 创建时间: 2017/10/20
 * 修改人:penghui.li
 * 修改时间:2017/10/20
 * 修改内容:
 *
 * 功能描述:与Android系统Socket基础通信
 */
public class SocketClient {
	private static final String SOCKET_NAME = "plcd-apps";
	private static final int TIMEOUT = 2500;

	private SocketClient() {
	}

	/**
	 * 发送消息
	 *
	 * @param data   要发送的数据
	 * @return 响应信息
	 */
	public static byte[] sendMsg(final byte[] data) {
		//创建socket
		LocalSocket client = new LocalSocket();
		LocalSocketAddress address = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace
				.RESERVED);

		//连接本地socket进程
		boolean isConnected = false;
		int connectCount = 0;
//					Log.e("socket_Test", "start connect!");
		while (!isConnected && connectCount < 10) {
			try {
				if (connectCount != 0) {
					Thread.sleep(20);
				}
				client.connect(address);
				isConnected = true;
			} catch (Exception e) {
				connectCount++;
				isConnected = false;
				Log.e("SocketClient", "Connect fail, err = " + e.toString());
			}
		}

		try {
			//防止未连接奔溃
			if (!client.isConnected()){
				//关闭 socket 并返回
				client.close();
				return new byte[0];
			}

			//设置读超时，防止一直阻塞
			client.setSoTimeout(TIMEOUT);

			//发送数据
//					Log.e("socket_Test", "start write!");
			if (data.length > 0) {
				client.getOutputStream().write(data);
			}

			//接收响应
//					Log.e("socket_Test", "start read!");
			byte[] result = readStream(client.getInputStream());

			//关闭输入流、输出流、socket
		/*	client.shutdownOutput();
			client.shutdownInput();*/
			client.close();

			return result;
		} catch (Exception e) {
			//socket读写操作失败
			Log.e("SocketClient", "SendMsg fail, err = " + e.toString());
			return new byte[0];
		}
	}

	/**
	 * 读取数据
	 *
	 * @param inStream 输入数据流
	 * @return 反回读取结果
	 * @throws Exception 读取异常
	 */
	private static byte[] readStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		return outSteam.toByteArray();

		/*int count = 0;
		while (count == 0) {
			count = inStream.available();
		}
		byte[] bytes = new byte[count];
		int readCount = 0; // 已经成功读取的字节的个数
		while (readCount < count) {
			readCount += inStream.read(bytes, readCount, count - readCount);
		}
		return bytes;*/
	}
}