package com.kawakp.kp.oxygenerator.model;

import android.view.View;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.adapter.DecimalNumWheelAdapter;
import com.kawakp.kp.oxygenerator.adapter.IntegerNumWheelAdapter;
import com.kawakp.kp.oxygenerator.interf.OnWheelChangedListener;
import com.kawakp.kp.oxygenerator.view.SetParamPopupWindow.Type;
import com.kawakp.kp.oxygenerator.widget.WheelView;

public class WheelParam {

	private View view;
	private WheelView wv_integer;
	private WheelView wv_decimal;
	public int screenheight;

	private Type type;
	private int START_INTEGER = 0, END_INTEGER = 120;
	private int START_DECIMAL = 0, END_DECIMAL = 99;
	private int MIN = 0; //当整数为最小值时，小数位的最小值
	private int MAX = 99;	//当整数位最大值时，小数位的最大值

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public int getStartInteger() {
		return START_INTEGER;
	}

	public int getEndInteger() {
		return END_INTEGER;
	}

	public int getStartDecimal() {
		return START_DECIMAL;
	}

	public int getEndDecimal() {
		return END_DECIMAL;
	}

	public int getMIN() {
		return MIN;
	}

	public void setMIN(int MIN) {
		this.MIN = MIN;
	}

	public int getMAX() {
		return MAX;
	}

	public void setMAX(int MAX) {
		this.MAX = MAX;
	}

	public WheelParam(View view) {
		super();
		this.view = view;
		type = Type.ALL;
		setView(view);
	}

	public WheelParam(View view, Type type) {
		super();
		this.view = view;
		this.type = type;
		setView(view);
	}

	/**
	 * @Description: 弹出参数选择器
	 */
	public void setPicker(int integer, int decimal) {
		// 整数
		wv_integer = (WheelView) view.findViewById(R.id.integer);
		wv_integer.setAdapter(new IntegerNumWheelAdapter(START_INTEGER, END_INTEGER));// 设置整数的显示数据
		switch (type) {
			case ALL:
				wv_integer.setLabel(".");// 添加文字
				break;
			case INTEGER:
				wv_integer.setLabel("");// 添加文字
				break;
		}
		wv_integer.setCurrentItem(integer - START_INTEGER);// 初始化整数显示的数据

		// 小数
		wv_decimal = (WheelView) view.findViewById(R.id.decimal);
		wv_decimal.setAdapter(new DecimalNumWheelAdapter(START_DECIMAL, END_DECIMAL));
		wv_decimal.setLabel("");
		wv_decimal.setCurrentItem(decimal - START_DECIMAL);

		wv_integer.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int num = newValue + START_INTEGER;
				if (num == START_INTEGER && MIN != START_DECIMAL){
					wv_decimal.setAdapter(new DecimalNumWheelAdapter(MIN, END_DECIMAL));
					if (MIN == END_DECIMAL){
						wv_decimal.setCyclic(false);
					}else {
						wv_decimal.setCyclic(true);
					}
				}else if (num == END_INTEGER && MAX != END_DECIMAL){
					wv_decimal.setAdapter(new DecimalNumWheelAdapter(START_DECIMAL, MAX));
					if (START_DECIMAL == MAX) {
						wv_decimal.setCyclic(false);
					}else {
						wv_decimal.setCyclic(true);
					}
				}else {
					wv_decimal.setAdapter(new DecimalNumWheelAdapter(START_DECIMAL, END_DECIMAL));
					if (START_DECIMAL == END_DECIMAL){
						wv_decimal.setCyclic(false);
					}else {
						wv_decimal.setCyclic(true);
					}
				}
			}
		});

		// 根据屏幕密度来指定选择器字体的大小(不同屏幕可能不同)
		int textSize = 0;
		switch (type) {
		case ALL:
			textSize = (screenheight / 100) * 3;
			break;
		case INTEGER:
			textSize = (screenheight / 100) * 4;
			wv_decimal.setVisibility(View.GONE);
			break;
		}

		wv_decimal.TEXT_SIZE = textSize;
		wv_integer.TEXT_SIZE = textSize;

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
		this.START_INTEGER = START_INTEGER;
		this.END_INTEGER = END_INTEGER;

		this.START_DECIMAL = START_DECIMAL;
		this.END_DECIMAL = END_DECIMAL;

		this.MIN = MIN;
		this.MAX = MAX;

		wv_integer.setAdapter(new IntegerNumWheelAdapter(START_INTEGER, END_INTEGER));// 设置整数的显示数据
		wv_decimal.setAdapter(new DecimalNumWheelAdapter(MIN, END_DECIMAL));
	}

	/**
	 * 设置可以整数选择的范围
	 *
	 * @param START_INTEGER
	 * @param END_INTEGER
	 */
	public void setIntegerRange(int START_INTEGER, int END_INTEGER)
	{
		this.START_INTEGER = START_INTEGER;
		this.END_INTEGER = END_INTEGER;

		wv_integer.setAdapter(new IntegerNumWheelAdapter(START_INTEGER, END_INTEGER));// 设置整数的显示数据
	}

	/**
	 * 设置是否循环滚动
	 * 
	 * @param cyclic
	 */
	public void setCyclic(boolean cyclic) {
		wv_integer.setCyclic(cyclic);
		wv_decimal.setCyclic(cyclic);
	}

	/**
	 * 获取选中参数
	 *
	 * @return
	 */
	public String getParam() {
		StringBuffer sb = new StringBuffer();

		switch (type) {
		case ALL:
			int intBit = wv_integer.getCurrentItem() + START_INTEGER;
			int floatBit = wv_decimal.getCurrentItem() + START_DECIMAL;
			//处理整数位是否处于最小位
			floatBit = intBit == START_INTEGER ? (floatBit + MIN):floatBit;
			sb.append(intBit).append(".")
					.append(String.format("%0" + String.valueOf(END_DECIMAL).length() + "d", floatBit));
			break;
		case INTEGER:
			sb.append((wv_integer.getCurrentItem() + START_INTEGER));
			break;
		}
		return sb.toString();
	}

}
