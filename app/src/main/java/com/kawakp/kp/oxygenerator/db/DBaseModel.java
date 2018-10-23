package com.kawakp.kp.oxygenerator.db;

/**
 * 所有自定义数据库表都需要基础这个类，
 * 且都必须有无参数的构造方法(列如 public DBaseModel(){})
 *
 * Created by penghui.li on 2017/6/24.
 */

public abstract class DBaseModel {
    //Sqlite 自增长ID
    public int _id;

    public int getId() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public abstract String toString();
}
