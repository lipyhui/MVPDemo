package com.kawakp.kp.oxygenerator.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import com.kawakp.kp.oxygenerator.view.TimePopupWindow;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TimeUtil {
	public static String selectTime = "选择时间";
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
	/** 时间选择popupwindow */
	public static void initSelectTimePopuwindow(Context context, final TextView button, boolean isBefore) {
		TimePopupWindow timePopupWindow = new TimePopupWindow(context, TimePopupWindow.Type.ALL);
		timePopupWindow.setCyclic(true);
		// 时间选择后回调
		timePopupWindow.setOnTimeSelectListener(new TimePopupWindow.OnTimeSelectListener() {

			@Override
			public void onTimeSelect(Date date) {
				selectTime = getTime(date);
				button.setText(selectTime);
			}
		});
		timePopupWindow.showAtLocation(button, Gravity.BOTTOM, 0, 0,new Date(), isBefore);
	}
	public static String getTime(Date date) {
		return format.format(date);
	}

}
