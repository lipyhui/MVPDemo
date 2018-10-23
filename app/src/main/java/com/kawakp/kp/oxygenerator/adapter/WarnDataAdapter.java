package com.kawakp.kp.oxygenerator.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.util.CheckUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by penghui.li on 2017/6/15.
 */

public class WarnDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Map<String, Object>> data = new ArrayList<>();

    public static enum ITEM_TYPE {
        ITEM_TYPE_WHITE,
        ITEM_TYPE_GRAY
    }

    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

    public WarnDataAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        if (data == null)
            return;
    }

    /**
     * 添加列表数据
     *
     * @param data
     * @param isReverse 是否需要数据翻转
     */
    public void addData(List<Map<String, Object>> data, boolean isReverse){
        if (null == data || data.size() <= 0)
            return;

		//数据翻转
		if (isReverse){
			Collections.reverse(data);
		}

        this.data.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * 添加列表数据到指定位置
     *
     * @param position
     * @param data
	 * @param isReverse 是否需要数据翻转
     */
    public void addData(int position, List<Map<String, Object>> data, boolean isReverse){
        if (null == data || data.size() <= 0)
            return;

		//数据翻转
		if (isReverse){
			Collections.reverse(data);
		}

        this.data.addAll(position, data);
        notifyDataSetChanged();
    }

	/**
	 * List数据翻转
	 */
	public void reverse(){
		Collections.reverse(data);
		notifyDataSetChanged();
	}

    /**
     * 清除列表数据
     */
    public void clearData(){
        data.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_WHITE.ordinal()) {
            return new WhiteHolder(mLayoutInflater.inflate(R.layout.item_white, parent, false));
        } else {
            return new GrayHolder(mLayoutInflater.inflate(R.layout.item_gray, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof WhiteHolder) {
            WhiteHolder item = (WhiteHolder) holder;
            item.itemTime.setText(data.get(position).get("time").toString());
            item.itemType.setText(data.get(position).get("type").toString());
            String dispelTime = data.get(position).get("timeRemove").toString();
            if (CheckUtil.isEmpty(dispelTime) || dispelTime.equals("0")){
                //item.itemTimeRemove.setText(mContext.getResources().getString(R.string.warn_no_dispel));
                item.itemTimeRemove.setText("预警中");
                item.itemTimeRemove.setTextColor(ContextCompat.getColor(mContext, R.color.colorWarnR));
            }else {
                item.itemTimeRemove.setText(dispelTime);
                item.itemTimeRemove.setTextColor(ContextCompat.getColor(mContext, R.color.colorHomeTitle));
            }

        } else if (holder instanceof GrayHolder) {
            GrayHolder item = (GrayHolder) holder;
            item.itemTime.setText(data.get(position).get("time").toString());
            item.itemType.setText(data.get(position).get("type").toString());
            String dispelTime = data.get(position).get("timeRemove").toString();
            if (CheckUtil.isEmpty(dispelTime) || dispelTime.equals("0")){
                //item.itemTimeRemove.setText(mContext.getResources().getString(R.string.warn_no_dispel));
                item.itemTimeRemove.setText("预警中");
                item.itemTimeRemove.setTextColor(ContextCompat.getColor(mContext, R.color.colorWarnR));
            }else {
                item.itemTimeRemove.setText(dispelTime);
                item.itemTimeRemove.setTextColor(ContextCompat.getColor(mContext, R.color.colorHomeTitle));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 == 0 ? ITEM_TYPE.ITEM_TYPE_GRAY.ordinal() : ITEM_TYPE.ITEM_TYPE_WHITE.ordinal();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class WhiteHolder extends RecyclerView.ViewHolder {

        //@BindView(R2.id.item_time)
        TextView itemTime;
       /* //@BindView(R2.id.item_level)
        TextView itemLevel;*/
        //@BindView(R2.id.item_text)
        TextView itemType;
        //@BindView(R2.id.item_time_remove)
        TextView itemTimeRemove;

        WhiteHolder(View view) {
            super(view);
//            ButterKnife.bind(this, view);

            itemTime = (TextView) view.findViewById(R.id.item_time);
            itemType = (TextView) view.findViewById(R.id.item_text);
            itemTimeRemove = (TextView) view.findViewById(R.id.item_time_remove);
        }
    }

    public static class GrayHolder extends RecyclerView.ViewHolder {

        //@BindView(R2.id.item_time)
        TextView itemTime;
    /*    //@BindView(R2.id.item_level)
        TextView itemLevel;*/
        //@BindView(R2.id.item_text)
        TextView itemType;
        //@BindView(R2.id.item_time_remove)
        TextView itemTimeRemove;

        GrayHolder(View view) {
            super(view);
//            ButterKnife.bind(this, view);

            itemTime = (TextView) view.findViewById(R.id.item_time);
            itemType = (TextView) view.findViewById(R.id.item_text);
            itemTimeRemove = (TextView) view.findViewById(R.id.item_time_remove);
        }
    }
}
