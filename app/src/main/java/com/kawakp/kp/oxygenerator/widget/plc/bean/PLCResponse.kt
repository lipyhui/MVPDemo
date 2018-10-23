package com.kawakp.kp.oxygenerator.widget.plc.bean

/**
 * 创建人: penghui.li
 * 创建时间: 2017/10/23
 * 修改人:penghui.li
 * 修改时间:2017/10/23
 * 修改内容:
 *
 * 功能描述:PLC读写响应数据
 *         respCode: 响应代码，默认为 -100
 *                   0：成功
 *                   -1：连接失败
 *                   -2：写数据失败
 *                   -3：响应接收失败
 *                   -4：校验失败
 *                   -5：数据解析失败
 *                   -100：未知原因失败
 *         respMsg: 响应提示消息
 *         data: 返回数据，默认长度为 0
 */
data class PLCResponse constructor(var respCode: Int = -100,
                                   var respMsg: String = "未知原因失败",
                                   val data: HashMap<String, PLCValue> = HashMap())