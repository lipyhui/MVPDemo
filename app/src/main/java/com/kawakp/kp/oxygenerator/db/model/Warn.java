package com.kawakp.kp.oxygenerator.db.model;

import com.kawakp.kp.oxygenerator.db.DBaseModel;

/**
 * 必须要 public Warn(){}
 *
 * Created by penghui.li on 2017/6/24.
 */

public class Warn  extends DBaseModel {

    //警告消息产生时间
    private String time;
    //报警类型  0.已经消除警报 1.压力高 2.浓度高 3.储氧量不足
    private int type;
    //消除警告时间
    private String dispelTime;

    public Warn(){}

    public Warn(String time, int type, String dispelTime){
        this.time = time;
        this.type = type;
        this.dispelTime = dispelTime;
    }

    public String getTime() {
        return time;
    }

    public int getType() {
        return type;
    }

    public String getdispelTime() {
        return dispelTime;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDispelTime(String dispelTime) {
        this.dispelTime = dispelTime;
    }

    @Override
    public String toString() {
        return "{ " +
                "_id = " + _id +
                ", time = " + time +
                ", type = " + type +
                ", dispelTime = " + dispelTime +
                " }";
    }
}
