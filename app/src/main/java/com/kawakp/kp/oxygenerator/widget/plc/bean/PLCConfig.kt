package com.kawakp.kp.oxygenerator.widget.plc.bean

/**
 * 创建人: penghui.li
 * 创建时间: 2017/11/22
 * 修改人:penghui.li
 * 修改时间:2017/11/22
 * 修改内容:
 *
 * 功能描述: PLC 配置表对应数据模板
 */
data class PLCConfig constructor(

        /**
         * 一项配置
         *
         *  @param type 数据类型
         *  @param area 内存区域
         *  @param dbNum 数据块数(位置)
         *  @param start 起始位置
         *  @param num 数据个数
         *  @param element 元件类型
         *  @param startAddr 元件起始地址
         */
        var type: String,
        var area: String,
        var dbNum: Int,
        var start: Int,
        var num: Int,
        var element: String,
        var startAddr: Int
)