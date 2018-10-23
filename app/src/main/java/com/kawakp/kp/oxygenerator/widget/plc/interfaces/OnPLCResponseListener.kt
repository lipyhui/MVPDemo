package com.kawakp.kp.oxygenerator.widget.plc.interfaces

import com.kawakp.kp.oxygenerator.widget.plc.bean.PLCResponse

/**
 * 创建人: penghui.li
 * 创建时间: 2017/10/26
 * 修改人:penghui.li
 * 修改时间:2017/10/26
 * 修改内容:
 *
 * 功能描述: PLC 读写数据监听，返回 PLC 读写结果
 */
interface OnPLCResponseListener {
    fun onPLCResponse(PLCResponse: PLCResponse)
}