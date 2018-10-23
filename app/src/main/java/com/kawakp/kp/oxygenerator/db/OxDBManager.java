package com.kawakp.kp.oxygenerator.db;

import android.content.ContentValues;
import android.content.Context;

import com.kawakp.kp.oxygenerator.db.model.Warn;
import com.kawakp.kp.oxygenerator.util.CheckUtil;

import java.util.List;

import io.reactivex.Observable;

/**
 * 针对制氧机的数据库操作管理
 * 数据库会在创建表时默认创建
 *
 * Created by penghui.li on 2017/6/28.
 */

public class OxDBManager {
    /**
     * 创建警告消息数据表
     */
    public static void createWarnTable(Context context){
        new DBManager.Builder(context)
                .build()
                .createTable(Warn.class);
    }

    /**
     * 添加一条警告消息
     *
     * @param context
     * @param time
     * @param type
     * @param dispelTime
     */
    public static void addWarnData(Context context, String time, int type, String dispelTime){
        new DBManager.Builder(context)
                .build()
                .addDataWithoutReturn(new Warn(time, type, dispelTime));
    }

    /**
     * 获取所有没有消除警报的数据
     *
     * @param context
     * @return
     */
    public static Observable<List<Warn>> getWarnDispelTime(Context context){
        return new DBManager.Builder(context)
                .build()
                .getDataSelectionWithoutOrder(Warn.class, "dispelTime = ? ", new String[]{"0"});
                //.getDataSelectionWithoutOrder(Warn.class, "time <> ?", new String[]{"2017/06/28 20:36"}); //不等于
    }

    /**
     * 消除一类报警信息
     *
     * @param context
     * @param dispelTime
     * @param type  支持 1、2、3
     */
    public static void dispelWarn(Context context, int type, String dispelTime){
        if (CheckUtil.isEmpty(dispelTime) || dispelTime.equals("0"))
            return;

        ContentValues values = new ContentValues();
        values.put("dispelTime", dispelTime);

        new DBManager.Builder(context)
                .build()
                .updateDateWithoutReturn(Warn.class, values, "type = ? and dispelTime = ? ", new String[]{String.valueOf(type), "0"});
    }

    /**
     * 获取所有的数据库所有的警告消息
     * 根据 _id 顺序排序
     *
     * @param context
     * @return
     */
    public static Observable<List<Warn>> getAllWarnAsc(Context context){
        return new DBManager.Builder(context)
                .build()
                .getData(Warn.class, "_id", SqlOperation.ORDER.ASC);
    }

    /**
     * 获取所有的数据库所有的警告消息
     * 根据 _id 倒序排序
     *
     * @param context
     * @return
     */
    public static Observable<List<Warn>> getAllWarnDesc(Context context){
        return new DBManager.Builder(context)
                .build()
                .getData(Warn.class, "_id", SqlOperation.ORDER.DESC);
    }
}
