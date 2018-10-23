package com.kawakp.kp.oxygenerator.constant;

/**
 * 创建人: penghui.li
 * 创建时间: 2017/8/9
 * 修改人:penghui.li
 * 修改时间:2017/8/9
 * 修改内容:
 *
 * 功能描述:
 */

public enum PageConstant {
	HOME(0),
	PROCESS(1),
	SET(2),
	DATA(3),
	WARN(4),
	PARAM(5),
	CONTROL(6),
	PASSWORD(7)
	;

	private int mPage = 0;
	PageConstant(int page){
		mPage = page;
	}

	public int getPage() {
		return mPage;
	}
}
