package com.kawakp.kp.oxygenerator.api;

import com.kawakp.kp.oxygenerator.constant.PageConstant;
import com.kawakp.kp.oxygenerator.util.CheckUtil;
import com.kawakp.kp.oxygenerator.util.Encrypt;
import com.kawakp.kp.oxygenerator.widget.plc.bean.PLCResponse;
import com.kawakp.kp.oxygenerator.widget.plc.kawa.Element;
import com.kawakp.kp.oxygenerator.widget.plc.kawa.PLCManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by penghui.li on 2017/6/21.
 */

public final class API {
	private API() {
	}

	/**
	 * 获取界面和登录状态
	 * D250 界面编码,想起见{@link PageConstant}
	 * D260 登录状态，1表示登录，0和其它表示未登录
	 */
	public static Observable<String[]> getPageAndLoginStatus() {
		return new PLCManager.ReadBuilder()
				.readWord(Element.WORD.D, 250)
				.readWord(Element.WORD.D, 260)
				.build()
				.start()
				.map(plcResponse -> {
					String[] strings = new String[2];
					strings[0] = String.valueOf(plcResponse.getData().get("D250").getWord());
					strings[1] = String.valueOf(plcResponse.getData().get("D260").getWord());
					return strings;
				});
	}

	/**
	 * 设置界面位置(编码)
	 * D250
	 */
	public static void setPage(final PageConstant page) {
		new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 250, page.getPage())
				.build()
				.startAsync();
	}

	/**
	 * 设置界登录状态编码)
	 * D260
	 */
	public static void setLoginStatus(final boolean status) {
		new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 260, status ? 1 : 0)
				.build()
				.startAsync();
	}

	/**
	 * 获取首页相关数据
	 *
	 * D620 含氧量--2位小数
	 * D511 温度--1位小数
	 * D512 湿度--1位小数
	 * D520 压力--1位小数
	 * D100 已运行时间--分
	 * D201 总运行时间--分
	 */
	public static Observable<String[]> getHomeData() {
		return new PLCManager.ReadBuilder()
				.readWord(Element.WORD.D, 620)
				.readWord(Element.WORD.D, 511)
				.readWord(Element.WORD.D, 512)
				.readWord(Element.WORD.D, 520)
				.readWord(Element.WORD.D, 100)
				.readWord(Element.WORD.D, 201)
				.build()
				.start()
				.map(plcResponse -> {
					String[] strings = new String[6];
					strings[0] = String.valueOf(plcResponse.getData().get("D620").getWord());
					strings[1] = String.valueOf(plcResponse.getData().get("D511").getWord());
					strings[2] = String.valueOf(plcResponse.getData().get("D512").getWord());
					strings[3] = String.valueOf(plcResponse.getData().get("D520").getWord());
					strings[4] = String.valueOf(plcResponse.getData().get("D100").getWord());
					strings[5] = String.valueOf(plcResponse.getData().get("D201").getWord());
					return strings;
				});
	}

	/**
	 * 获取首页设备状态信息
	 *
	 * Y0 加压
	 * Y1 弥散氧
	 * Y2 呼吸氧
	 * Y3 卸压
	 * Y4 卸压
	 */
	public static Observable<Boolean[]> getHomeDeviceStatus() {
		return new PLCManager.ReadBuilder()
				.readBool(Element.BOOL.Y, 0)
				.readBool(Element.BOOL.Y, 1)
				.readBool(Element.BOOL.Y, 2)
				.readBool(Element.BOOL.Y, 3)
				.readBool(Element.BOOL.Y, 4)
				.build()
				.start()
				.map(plcResponse -> {
					Boolean[] strings = new Boolean[4];
					strings[0] = plcResponse.getData().get("Y0").getBool();
					strings[1] = plcResponse.getData().get("Y1").getBool();
					strings[2] = plcResponse.getData().get("Y2").getBool();

					if (plcResponse.getData().get("Y3").getBool() || plcResponse.getData().get("Y4").getBool()) {
						strings[3] = true;
					} else {
						strings[3] = false;
					}
					return strings;
				});
	}

	/**
	 * 获取首页其他设备的状态信息
	 *
	 * M320 设备启动/停止
	 * M304 灯
	 * M303 门
	 */
	public static Observable<Boolean[]> getHomeOtherStatus() {
		return new PLCManager.ReadBuilder()
				.readBool(Element.BOOL.M, 320)
				.readBool(Element.BOOL.M, 304)
				.readBool(Element.BOOL.M, 303)
				.build()
				.start()
				.map(plcResponse -> {
					Boolean[] strings = new Boolean[3];
					strings[0] = plcResponse.getData().get("M320").getBool();
					strings[1] = plcResponse.getData().get("M304").getBool();
					strings[2] = plcResponse.getData().get("M303").getBool();
					return strings;
				});
	}

	/**
	 * 控制设备启动/停止
	 *
	 * D400
	 *
	 * @param status true:开启设备;  false:关闭设备
	 * @return true:发送完成;  false:发送失败
	 */
	public static Observable<Boolean> setDeviceStatus(final boolean status) {
		return new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 400, status ? 1 : 0)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getRespCode() == 0);
	}

	/**
	 * 控制灯的开关
	 *
	 * D141
	 *
	 * @param status true:开;  false:关
	 * @return true:发送完成;  false:发送失败
	 */
	public static Observable<Boolean> setLampStatus(final boolean status) {
		return new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 141, status ? 1 : 0)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getRespCode() == 0);
	}

	/**
	 * 控制门的开关
	 *
	 * D140
	 *
	 * @param status true:开;  false:关
	 * @return true:发送完成;  false:发送失败
	 */
	public static Observable<Boolean> setDoorStatus(final boolean status) {
		return new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 140, status ? 1 : 0)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getRespCode() == 0);
	}

	/**
	 * 获取流程拟态界面舱室压力和浓度
	 *
	 * D530 储气罐压力
	 * D620 浓度
	 *
	 * @return 数组下标 [0] 为压力，数组下标 [1] 为浓度。
	 */
	public static Observable<String[]> getGasholderPAndCon() {
		return new PLCManager.ReadBuilder()
				.readWord(Element.WORD.D, 530)
				.readWord(Element.WORD.D, 620)
				.build()
				.start()
				.map(plcResponse -> {
					String[] strings = new String[2];
					strings[0] = String.valueOf(plcResponse.getData().get("D530").getWord());
					strings[1] = String.valueOf(plcResponse.getData().get("D620").getWord());
					return strings;
				});
	}

	/**
	 * 判断空压机是否启动
	 *
	 * Y7
	 *
	 * @return true:已经启动；false:未启动
	 */
	public static Observable<Boolean> isCompressStart() {
		return new PLCManager.ReadBuilder()
				.readBool(Element.BOOL.Y, 7)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getData().get("Y7").getBool());
	}

	/**
	 * 判断冷干机是否启动
	 *
	 * Y7
	 *
	 * @return true:已经启动；false:未启动
	 */
	public static Observable<Boolean> isCondenserStart() {
		return new PLCManager.ReadBuilder()
				.readBool(Element.BOOL.Y, 7)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getData().get("Y7").getBool());
	}

	/**
	 * 获取设置相关参数
	 *
	 * D203 室内压力
	 * D204 总运行时间
	 * D205 氧气浓度
	 */
	public static Observable<String[]> getSetData() {
		return new PLCManager.ReadBuilder()
				.readWord(Element.WORD.D, 203)
				.readWord(Element.WORD.D, 204)
				.readWord(Element.WORD.D, 205)
				.build()
				.start()
				.map(plcResponse -> {
					String[] strings = new String[3];
					strings[0] = String.valueOf(plcResponse.getData().get("D203").getWord());
					strings[1] = String.valueOf(plcResponse.getData().get("D204").getWord());
					strings[2] = String.valueOf(plcResponse.getData().get("D205").getWord());
					return strings;
				});
	}

	/**
	 * 设置室内压力
	 *
	 * D203
	 *
	 * @param value 数字字符串
	 * @return true:发送完成;  false:发送失败
	 */
	public static Observable<Boolean> setPValue(final int value) {
		return new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 203, value)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getRespCode() == 0);
	}

	/**
	 * 设置运行时间
	 *
	 * D204
	 *
	 * @param value 数字字符串
	 * @return true:发送完成;  false:发送失败
	 */
	public static Observable<Boolean> setTimeValue(final int value) {
		return new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 204, value)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getRespCode() == 0);
	}

	/**
	 * 设置氧气浓度
	 *
	 * D205
	 *
	 * @param value 数字字符串
	 * @return true:发送完成;  false:发送失败
	 */
	public static Observable<Boolean> setConValue(final int value) {
		return new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 205, value)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getRespCode() == 0);
	}

	/**
	 * 获取设置运行相关数据
	 *
	 * D201 总运行时间
	 * D620 氧气浓度--2位小数位
	 * D515 室内压力--1位小数位
	 * D512 室内湿度--1位小数位
	 * D511 室内温度-1位小数位
	 */
	public static Observable<String[]> getSetRunData() {
		return new PLCManager.ReadBuilder()
				.readWord(Element.WORD.D, 201)
				.readWord(Element.WORD.D, 620)
				.readWord(Element.WORD.D, 515)
				.readWord(Element.WORD.D, 512)
				.readWord(Element.WORD.D, 511)
				.build()
				.start()
				.map(plcResponse -> {
					String[] strings = new String[5];
					strings[0] = String.valueOf(plcResponse.getData().get("D201").getWord());
					strings[1] = String.valueOf(plcResponse.getData().get("D620").getWord());
					strings[2] = String.valueOf(plcResponse.getData().get("D515").getWord());
					strings[3] = String.valueOf(plcResponse.getData().get("D512").getWord());
					strings[4] = String.valueOf(plcResponse.getData().get("D511").getWord());
					return strings;
				});
	}

	/**
	 * 获取控制设置开关状态
	 *
	 * M307   加压
	 * M313   弥散氧
	 * M308   呼吸氧
	 * M311   卸压
	 */
	public static Observable<Boolean[]> getSetControlStatus() {
		return new PLCManager.ReadBuilder()
				.readBool(Element.BOOL.M, 307)
				.readBool(Element.BOOL.M, 313)
				.readBool(Element.BOOL.M, 308)
				.readBool(Element.BOOL.M, 311)
				.build()
				.start()
				.map(plcResponse -> {
					Boolean[] strings = new Boolean[4];
					strings[0] = plcResponse.getData().get("M307").getBool();
					strings[1] = plcResponse.getData().get("M313").getBool();
					strings[2] = plcResponse.getData().get("M308").getBool();
					strings[3] = plcResponse.getData().get("M311").getBool();
					return strings;
				});
	}

	/**
	 * 控制加压开关
	 *
	 * D142
	 *
	 * @param status true:开;  false:关
	 * @return true:发送完成;  false:发送失败
	 */
	public static Observable<Boolean> setAddPStatus(final boolean status) {
		return new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 142, status ? 1 : 0)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getRespCode() == 0);
	}

	/**
	 * 控制弥散氧开关
	 *
	 * D148
	 *
	 * @param status true:开;  false:关
	 * @return true:发送完成;  false:发送失败
	 */
	public static Observable<Boolean> setDispersionStatus(final boolean status) {
		return new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 148, status ? 1 : 0)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getRespCode() == 0);
	}

	/**
	 * 控制呼吸氧开关
	 *
	 * D143
	 *
	 * @param status true:开;  false:关
	 * @return true:发送完成;  false:发送失败
	 */
	public static Observable<Boolean> setBreathStatus(final boolean status) {
		return new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 143, status ? 1 : 0)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getRespCode() == 0);
	}

	/**
	 * 控制卸压开关
	 *
	 * D146
	 *
	 * @param status true:开;  false:关
	 * @return true:发送完成;  false:发送失败
	 */
	public static Observable<Boolean> setDelPStatus(final boolean status) {
		return new PLCManager.WriteBuilder()
				.writeWord(Element.WORD.D, 146, status ? 1 : 0)
				.build()
				.start()
				.map(plcResponse -> plcResponse.getRespCode() == 0);
	}

	/**
	 * 获取当前的数据和原始数据
	 *
	 * D610 氧气浓度(原始数据--0位小数位)
	 * D513 室内压力(原始数据--0位小数位)
	 * D511 室内温度(原始数据--0位小数位，当前数据--1位小数位)
	 * D512 室内湿度(原始数据--0位小数位，当前数据--1位小数位)
	 * D530 罐体压力(原始数据--0位小数位，当前数据--1位小数位)
	 * D515 氧舱压力(原始数据--0位小数位，当前数据--1位小数位)
	 * D620 氧气浓度(当前数据--2位小数位)
	 * D515 室内压力(当前数据--1位小数位)
	 */
	public static Observable<String[]> getSensorData() {
		return new PLCManager.ReadBuilder()
				.readWord(Element.WORD.D, 610)
				.readWord(Element.WORD.D, 513)
				.readWord(Element.WORD.D, 511)
				.readWord(Element.WORD.D, 512)
				.readWord(Element.WORD.D, 530)
				.readWord(Element.WORD.D, 515)
				.readWord(Element.WORD.D, 620)
				.readWord(Element.WORD.D, 515)
				.build()
				.start()
				.map(plcResponse -> {
					String[] strings = new String[8];
					strings[0] = String.valueOf(plcResponse.getData().get("D610").getWord());
					strings[1] = String.valueOf(plcResponse.getData().get("D513").getWord());
					strings[2] = String.valueOf(plcResponse.getData().get("D511").getWord());
					strings[3] = String.valueOf(plcResponse.getData().get("D512").getWord());
					strings[4] = String.valueOf(plcResponse.getData().get("D530").getWord());
					strings[5] = String.valueOf(plcResponse.getData().get("D515").getWord());
					strings[6] = String.valueOf(plcResponse.getData().get("D620").getWord());
					strings[7] = String.valueOf(plcResponse.getData().get("D515").getWord());
					return strings;
				});
	}

	/**
	 * 获取报警状态信息
	 *
	 * M1000   压力--true:高;  false:正常
	 * M1001   浓度--true:高;  false:正常
	 * M1002   储氧量--true:不足;  false:正常
	 */
	public static Observable<Boolean[]> getWarnStatus() {
		return new PLCManager.ReadBuilder()
				.readBool(Element.BOOL.M, 1000)
				.readBool(Element.BOOL.M, 1001)
				.readBool(Element.BOOL.M, 1002)
				.build()
				.start()
				.map(plcResponse -> {
					Boolean[] strings = new Boolean[3];
					strings[0] = plcResponse.getData().get("M1000").getBool();
					strings[1] = plcResponse.getData().get("M1001").getBool();
					strings[2] = plcResponse.getData().get("M1002").getBool();
					return strings;
				});
	}

	/**
	 * 获取PLC时间
	 *
	 * SD100    年
	 * SD101    月
	 * SD102    日
	 * SD103    时
	 * SD104    分
	 * SD105    秒
	 * SD106    星期
	 */
	public static Observable<String> getTime() {
		return new PLCManager.ReadBuilder()
				.readWord(Element.WORD.SD, 100)
				.readWord(Element.WORD.SD, 101)
				.readWord(Element.WORD.SD, 102)
				.readWord(Element.WORD.SD, 103)
				.readWord(Element.WORD.SD, 104)
				.readWord(Element.WORD.SD, 105)
				.readWord(Element.WORD.SD, 106)
				.build()
				.start()
				.map(plcResponse -> {
					String[] response = new String[7];
					response[0] = String.valueOf(plcResponse.getData().get("SD100").getWord());
					response[1] = String.valueOf(plcResponse.getData().get("SD101").getWord());
					response[2] = String.valueOf(plcResponse.getData().get("SD102").getWord());
					response[3] = String.valueOf(plcResponse.getData().get("SD103").getWord());
					response[4] = String.valueOf(plcResponse.getData().get("SD104").getWord());
					response[5] = String.valueOf(plcResponse.getData().get("SD105").getWord());
					response[6] = String.valueOf(plcResponse.getData().get("SD106").getWord());

					StringBuffer buffer = new StringBuffer();

					buffer.append(Integer.parseInt(response[0]) <= 0 ? "2017" : response[0]);
					buffer.append("/");
					buffer.append(Integer.parseInt(response[1]) < 10 ? "0" : "");
					buffer.append(response[1]);
					buffer.append("/");
					buffer.append(Integer.parseInt(response[2]) < 10 ? "0" : "");
					buffer.append(response[2]);
					buffer.append(" ");
					buffer.append(Integer.parseInt(response[3]) < 10 ? "0" : "");
					buffer.append(response[3]);
					buffer.append(":");
					buffer.append(Integer.parseInt(response[4]) < 10 ? "0" : "");
					buffer.append(response[4]);
					buffer.append(" ");

					switch (response[6]) {
						case "1":
							buffer.append("星期一");
							break;

						case "2":
							buffer.append("星期二");
							break;

						case "3":
							buffer.append("星期三");
							break;

						case "4":
							buffer.append("星期四");
							break;

						case "5":
							buffer.append("星期五");
							break;

						case "6":
							buffer.append("星期六");
							break;

						case "7":
							buffer.append("星期日");
							break;

						default:
							buffer.append("星期一");
							break;
					}

					return buffer.toString();
				});
	}

	/**
	 * 存储密保问题和答案,首位密码和第二位密码
	 *
	 * D410~ D449 存储密保问题
	 * D460~ D499 存储密保问题的答案
	 * D406、D407 存储首位密码
	 * D408、D409 存储第二位位密码
	 */
	public static Observable<Boolean> saveQuestion(final String question, final String answer, final String
			passwordOne, final String passWordTwo) {
		return Observable.just(new String[]{question, answer, passwordOne, passWordTwo})
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.io())
				.map(strings -> {
					if (CheckUtil.isEmpty(strings[0]) || CheckUtil.isEmpty(strings[1])) {
						return null;
					}
					//Log.e("API_string", "s0 = " + strings[0]);
					//Log.e("API_string", "s1 = " + strings[1]);
					//问题加密
					String encryptQ = Encrypt.AES("keyQ", strings[0]);
					//答案加密
					String encryptA = Encrypt.AES("keyA", strings[1]);
					//String decrypt = Encrypt.decryptAES("key", encrypt);
						 /*   Log.e("LaunchTest", "encryptQ = " + encryptQ);
							Log.e("LaunchTest", "encryptA = " + encryptA);*/
                       /*     Log.e("APIGetQuestion", "encryptQ decrypt = " + Encrypt.decryptAES("keyQ", encryptQ));
							Log.e("APIGetQuestion", "encryptA decrypt = " + Encrypt.decryptAES("keyA", encryptA));*/
					List<Element.ElementWORD> data = new ArrayList<>();
					int questionLength = encryptQ.length();
					int answerLength = encryptA.length();
					//问题转换
					for (int i = 0; i < 40; i++) {
						if ((i * 2) < questionLength) {
							String sub = encryptQ.substring(i * 2,
									(((i * 2 + 2) >= questionLength) || (questionLength == 1)) ? questionLength
											: (i * 2 + 2));
							int ox = Integer.parseInt(sub, 16);
							//Log.e("APIGetQuestion", "question addrs = " + addrs[i] + ", sub = " + sub + ", ox =
							// " + ox);
							data.add(new Element.ElementWORD(Element.WORD.D, 410 + i, ox));
						} else {
							data.add(new Element.ElementWORD(Element.WORD.D, 410 + i, 920));    // "\0" 结束标志符
							break;
						}
					}
					//答案转换
					for (int i = 0; i < 40; i++) {
						if ((i * 2) < answerLength) {
							String sub = encryptA.substring(i * 2,
									(((i * 2 + 2) >= answerLength) || (answerLength == 1)) ? answerLength
											: (i * 2 + 2));
							int ox = Integer.parseInt(sub, 16);
							//Log.e("APIGetQuestion", "answer  addrs = " + addrs[40 + i] + ", sub = " + sub + ",
							// ox = " + ox);
							data.add(new Element.ElementWORD(Element.WORD.D, 460 + i, ox));
						} else {
							data.add(new Element.ElementWORD(Element.WORD.D, 460 + i, 920));    // "\0" 结束标志符
							break;
						}
					}
                  /*          for (int i = 0; i < 84; i++)
								Log.e("API_SUB", addrs[i] + " = " + strs[i]);*/
					//保存首位密码
					data.add(
							new Element.ElementWORD(Element.WORD.D, 406, Integer.parseInt(strings[2].substring(0, 4)
							)));
					data.add(
							new Element.ElementWORD(Element.WORD.D, 407, Integer.parseInt(strings[2].substring(4, 7)
							)));
					//保存第二位位密码
					data.add(
							new Element.ElementWORD(Element.WORD.D, 408, Integer.parseInt(strings[2].substring(0, 4)
							)));
					data.add(
							new Element.ElementWORD(Element.WORD.D, 409, Integer.parseInt(strings[2].substring(4, 7)
							)));
                     /*       Log.e("APIGetQuestion", "420 = " + strs[80]);
                            Log.e("APIGetQuestion", "421 = " + strs[81]);
                            Log.e("APIGetQuestion", "422 = " + strs[82]);
                            Log.e("APIGetQuestion", "423 = " + strs[83]);*/
					//问题和答案数据存储
					return new PLCManager.WriteBuilder()
							.writeWordList(data)
							.build()
							.startSync()
							.getRespCode() == 0;
					//Log.e("APIGetQuestion", "qAndA write");
				});
	}

	/**
	 * 获取密保问题和答案
	 *
	 * @return s[0]:问题 s[1]:答案
	 */
	public static Observable<String[]> getQuestion() {
		return Observable.just(84)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.io())
				.map(it -> {
					List<Element.ElementWORD> list = new ArrayList<>();
					for (int i = 0; i < 40; i++) {
						list.add(new Element.ElementWORD(Element.WORD.D, 410 + i));
						list.add(new Element.ElementWORD(Element.WORD.D, 460 + i));
					}
					list.add(new Element.ElementWORD(Element.WORD.D, 406));
					list.add(new Element.ElementWORD(Element.WORD.D, 407));
					list.add(new Element.ElementWORD(Element.WORD.D, 408));
					list.add(new Element.ElementWORD(Element.WORD.D, 409));
					//Log.e("APIGetQuestion", "Map");
					PLCResponse response = new PLCManager.ReadBuilder()
							.readWordList(list)
							.build()
							.startSync();
					String[] result = new String[4];
					if (response.getRespCode() != 0) {    //防止数据读取失败
						result[0] = "";
						result[1] = "";
						result[2] = "";
						result[3] = "";
						return result;
					}
					//Log.e("APIGetQuestion", "Read");
					StringBuffer question = new StringBuffer();
					StringBuffer answer = new StringBuffer();
					//组合问题
					for (int i = 0; i < 40; i++) {
						int data = response.getData().get("D" + (410 + i)).getWord();
						if (data == 920) {
							break;
						} else {
							question.append(
									data < 16 ? ("0" + Integer.toHexString(data)) : Integer.toHexString(data));
						}
					}
					//Log.e("APIGetQuestion", "Question");
					//组合答案
					for (int i = 0; i < 40; i++) {
						int data = response.getData().get("D" + (460 + i)).getWord();
						if (data == 920) {
							break;
						} else {
							answer.append(
									data < 16 ? ("0" + Integer.toHexString(data)) : Integer.toHexString(data));
						}
					}
					//Log.e("APIGetQuestion", "answer");
					try {
						result[0] = Encrypt.decryptAES("keyQ", question.toString());
						result[1] = Encrypt.decryptAES("keyA", answer.toString());
						result[2] = response.getData().get("D406").getWord() + "" + response.getData().get(
								"D407").getWord();
						result[3] = response.getData().get("D408").getWord() + "" + response.getData().get(
								"D409").getWord();
					} catch (Exception e) {
						result[0] = "";
						result[1] = "";
						result[2] = "";
						result[3] = "";
					}
					//Log.e("APIGetQuestion", result[0]);
					//Log.e("APIGetAnswer", result[1]);
					return result;
				});
	}
}
