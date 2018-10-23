package com.kawakp.kp.oxygenerator.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.model.WheelParam;
import com.kawakp.kp.oxygenerator.util.ScreenInfo;

public class SetParamPopupWindow extends PopupWindow implements OnClickListener {

	public enum Type
	{
		ALL, INTEGER
	}

	private View rootView;
	WheelParam wheelParam;
	private View btnSubmit, btnCancel;
	private static final String TAG_SUBMIT = "submit";
	private static final String TAG_CANCEL = "cancel";
	private OnParamSelectListener paramSelectListener;

	@SuppressLint("InflateParams")
	@SuppressWarnings("deprecation")
	public SetParamPopupWindow(Context context, Type type)
	{
		super(context);
		this.setWidth(LayoutParams.FILL_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(new BitmapDrawable());// 这样设置才能点击屏幕外dismiss窗口
		this.setOutsideTouchable(true);
		this.setAnimationStyle(R.style.timepopwindow_anim_style);

		LayoutInflater mLayoutInflater = LayoutInflater.from(context);
		rootView = mLayoutInflater.inflate(R.layout.set_param_picker, null);
		// -----确定和取消按钮
		btnSubmit = rootView.findViewById(R.id.btnSubmit);
		btnSubmit.setTag(TAG_SUBMIT);
		btnCancel = rootView.findViewById(R.id.btnCancel);
		btnCancel.setTag(TAG_CANCEL);
		btnSubmit.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		// ----时间转轮
		final View parampickerview = rootView.findViewById(R.id.param_picker);
		ScreenInfo screenInfo = new ScreenInfo((Activity) context);
		wheelParam = new WheelParam(parampickerview, type);

		wheelParam.screenheight = screenInfo.getHeight();

		wheelParam.setPicker(0, 0);

		setContentView(rootView);
	}

	/**
	 * 设置可以选择的参数范围
	 *
	 * @param START_INTEGER
	 * @param END_INTEGER
	 * @param START_DECIMAL
	 * @param END_DECIMAL
	 */
	public void setRange(int START_INTEGER, int END_INTEGER, int START_DECIMAL, int END_DECIMAL, int MIN, int MAX)
	{
		wheelParam.setRange(START_INTEGER, END_INTEGER, START_DECIMAL, END_DECIMAL, MIN, MAX);
	}

	/**
	 * 设置可以整数选择的范围
	 *
	 * @param START_INTEGER
	 * @param END_INTEGER
	 */
	public void setIntegerRange(int START_INTEGER, int END_INTEGER)
	{
		wheelParam.setIntegerRange(START_INTEGER, END_INTEGER);
	}

	/**
	 * 当整数为最小值时，小数位的最小值
	 *
	 * @param min
	 */
/*	public void setMin(int min){
		wheelParam.setMIN(min);
	}

	*//**
	 * 当整数位最大值时，小数位的最大值
	 *
	 * @param max
	 *//*
	public void setMax(int max){
		wheelParam.setMAX(max);
	}*/

	/**
	 * 指定选中的参数，显示选择器
	 *
	 * @param parent
	 * @param gravity
	 * @param x
	 * @param y
	 * @param integer
	 * @param decimal
	 */
	public void showAtLocation(View parent, int gravity, int x, int y, int integer, int decimal)
	{
		wheelParam.setPicker(integer, decimal);
		update();
		super.showAtLocation(parent, gravity, x, y);
	}


	/**
	 * 设置选中参数
	 *
	 * @param integer	整数部分
	 * @param decimal	小数部分
	 */
	public void setTime(int integer, int decimal)
	{
		wheelParam.setPicker(integer, decimal);
	}

	/**
	 * 设置是否循环滚动
	 * 
	 * @param cyclic
	 */
	public void setCyclic(boolean cyclic)
	{
		wheelParam.setCyclic(cyclic);
	}

	@Override
	public void onClick(View v)
	{
		String tag = (String) v.getTag();
		if (tag.equals(TAG_CANCEL))
		{
			dismiss();
			return;
		} else
		{
			if (paramSelectListener != null)
			{
				paramSelectListener.onParamSelect(wheelParam.getParam());
			}
			dismiss();
			return;
		}
	}

	public interface OnParamSelectListener
	{
		public void onParamSelect(String data);
	}

	public void setOnParamSelectListener(OnParamSelectListener paramSelectListener)
	{
		this.paramSelectListener = paramSelectListener;
	}


}
