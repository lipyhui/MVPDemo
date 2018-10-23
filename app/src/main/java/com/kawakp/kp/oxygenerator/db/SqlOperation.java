package com.kawakp.kp.oxygenerator.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kawakp.kp.oxygenerator.util.CheckUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by penghui.li on 2017/6/24.
 */

public class SqlOperation<T extends DBaseModel> {
    private String TAG = this.getClass().getSimpleName();

    public enum ORDER{
        NONE,   //不排序
        ASC,    //升序排序
        DESC    //降序排序
    }

    /**
     * 更具数据模型创建数据表，可一次创建多个数据表
     *
     * @param dbHelper
     * @param classes
     */
    public void createTable(SQLiteOpenHelper dbHelper, Class<T>... classes){
        if (null == dbHelper || classes.length <= 0)
            return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        StringBuffer sqlBuff = new StringBuffer();

        for (Class cls:classes){
            sqlBuff.append(addPrimaryKey());

            for (Field field:cls.getDeclaredFields())
                sqlBuff.append(Type2Sqlite(field));

            sqlBuff.deleteCharAt(sqlBuff.length() - 1);
            sqlBuff.append(")");
            if (isLegalSql(sqlBuff.toString())){
                sqlBuff.insert(0, cls.getSimpleName());
                sqlBuff.insert(0, "CREATE TABLE IF NOT EXISTS ");
                db.execSQL(sqlBuff.toString());
            }

            sqlBuff.setLength(0);
        }
        closeAll(null, dbHelper, db);
    }

    /**
     * 插入一组数据到数据库
     *
     * @param dbHelper
     * @param list
     * @return
     */
    public List<Boolean> insert(SQLiteOpenHelper dbHelper, List<T> list){
        List<Boolean> result = new ArrayList<>();
        for (T t:list)
            result.add(insert(dbHelper, t));

        return result;
    }

    /**
     * 写入一条数据到数据库
     *
     * @param dbHelper
     * @param t
     * @return  true:数据插入成功 false:输入插入失败
     */
    public boolean insert(SQLiteOpenHelper dbHelper, T t){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        Class cls = t.getClass();

        for (Field field:cls.getDeclaredFields())
            values.putAll(putValue(field, t));

        long result = -1;
        try {
            result = db.insert(cls.getSimpleName(), null, values);
        } catch (Exception e) {
            // TODO: handle exception
        }

        closeAll(null, dbHelper, db);

        return  result != -1;
    }

    /**
     * 根据条件更新数据库里 Class<T> 表的数据
     *
     * @param dbHelper
     * @param cls
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public int update(SQLiteOpenHelper dbHelper, Class<T> cls, ContentValues values, String whereClause, String[] whereArgs){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int result = 0;
        try {
            result = db.update(cls.getSimpleName(), values, whereClause, whereArgs);
        }catch (Exception e){
            e.printStackTrace();
        }

        closeAll(null, dbHelper, db);

        return result;
    }

    /**
     * 更新数据库里的一个表(即添加列、删除列)
     *
     * @param dbHelper
     * @param cls
     * @return
     */
    /*public int updateTable(SQLiteOpenHelper dbHelper, Class<T> cls){
    }*/

    /**
     * 根据条件删除一条数据
     *
     * @param dbHelper
     * @param cls
     * @param whereClause   SQLite 条件语句
     * @param whereArgs     删除满足的条件
     * @return
     */
    public boolean delete(SQLiteOpenHelper dbHelper, Class<T> cls, String whereClause, String[] whereArgs){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int isDelete;

        isDelete = db.delete(cls.getSimpleName(), whereClause, whereArgs);

        closeAll(null, dbHelper, db);

        return isDelete > 0;
    }

    /**
     * 删除 Class<T> 表里面的所有数据
     *
     * @param dbHelper
     * @param cls
     * @return
     */
    public boolean deleteAll(SQLiteOpenHelper dbHelper, Class<T> cls){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int doneDelete = 0;

        try {
            doneDelete = db.delete(cls.getSimpleName(), null, null);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        closeAll(null, dbHelper, db);

        return doneDelete > 0;
    }

    /**
     * 获取 Class<T>表里保存的数据条数
     *
     * @param dbHelper
     * @param cls
     * @return
     */
    public int getCount(SQLiteOpenHelper dbHelper, Class<T> cls){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        int count = 0;

        Cursor cursor = db.query(cls.getSimpleName(), null, null, null, null, null, null);
        count = cursor.getCount();

        closeAll(cursor, dbHelper, db);

        return count;
    }

    /**
     * 根据条件查询数，可选择是否排序
     *
     * @param dbHelper
     * @param cls   表名
     * @param columns   要查找的列
     * @param selection 查找条件
     * @param selectionArgs 查找满足条件
     * @param by      排序列名
     * @param order     排序方式
     * @return
     */
    public List<T> query(SQLiteOpenHelper dbHelper, Class<T> cls, String[] columns, String selection,
                         String[] selectionArgs, String by, ORDER order){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = null;
        if (null == by || order == null || order == ORDER.NONE) {
            cursor = db.query(cls.getSimpleName(), columns, selection, selectionArgs, null, null, null);
        }else {
            cursor = db.query(cls.getSimpleName(), columns, selection, selectionArgs, null, null, by + order);
        }

        List<T> result = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()){
            do {
                try {
                    Object obj = cls.newInstance();

                    /**
                     * 设置 {@link DBaseModel#_id},即数据库的自增 ID
                     * */
                    Field fieldSet = cls.getSuperclass().getDeclaredField("_id"); //获取对象
                    //设置为public
                    fieldSet.setAccessible(true);
                    fieldSet.setInt(obj, cursor.getInt(cursor.getColumnIndex("_id")));

                    //设置自定义 model 的变量值
                    for (Field field:cls.getDeclaredFields())
                        obj = getValue(field, cursor, cls, obj);
                    result.add((T) obj);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }while (cursor.moveToNext());
        }

        closeAll(cursor, dbHelper, db);

        return result;
    }

    /**
     * 查询所有数据，不排序
     *
     * @param dbHelper
     * @param cls
     * @return
     */
    public List<T> fetchAll(SQLiteOpenHelper dbHelper, Class<T> cls){
        return fetchAll(dbHelper, cls, null, null);
    }

    /**
     * 查询所有的数据，根据 by 这一列进行升序或者降序排序
     *
     * @param dbHelper
     * @param cls
     * @param by
     * @param order
     * @return
     */
    public List<T> fetchAll(SQLiteOpenHelper dbHelper, Class<T> cls, String by, ORDER order){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        if (CheckUtil.isEmpty(by) || null == order){
            cursor = db.query(cls.getSimpleName(), null, null, null, null, null, null);
        }else {
            switch (order) {
                case NONE:
                    cursor = db.query(cls.getSimpleName(), null, null, null, null, null, null);
                    break;

                case ASC:
                    cursor = db.query(cls.getSimpleName(), null, null, null, null, null, by + " asc");
                    break;

                case DESC:
                    cursor = db.query(cls.getSimpleName(), null, null, null, null, null, by + " desc");
                    break;

                default:
                    break;
            }
        }

        List<T> result = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()){
            do {
                try {
                    Object obj = cls.newInstance();

                    /**
                     * 设置 {@link DBaseModel#_id},即数据库的自增 ID
                     * */
                    Field fieldSet = cls.getSuperclass().getDeclaredField("_id"); //获取对象
                    //设置为public
                    fieldSet.setAccessible(true);
                    fieldSet.setInt(obj, cursor.getInt(cursor.getColumnIndex("_id")));

                    //设置自定义 model 的变量值
                    for (Field field:cls.getDeclaredFields())
                        obj = getValue(field, cursor, cls, obj);
                    result.add((T) obj);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }while (cursor.moveToNext());
        }

        closeAll(cursor, dbHelper, db);

        return result;
    }

    /**
     * 根据 Android 反射读取的数据模型变量
     * 转换为 Sqlite 语句
     *
     * @param field
     * @return
     */
    private String Type2Sqlite(Field field){

        //设置为public
        field.setAccessible(true);

        switch (field.getGenericType().toString()){
            case "byte":
            case "short":
            case "long":
            case "float":
            case "double":
            case "class java.lang.String":
            case "boolean":
                return addText(field.getName());

            case "int":
                return addInteger(field.getName());

       /*     case "boolean":
                return addBlob(field.getName());*/

            default:
                break;
        }
        return "";
    }

    /**
     * 根据 Android 反射的数据模型变量
     * 转换为对表赋值的语句
     *
     * @return
     */
    private ContentValues putValue(Field field, T t){
        ContentValues values = new ContentValues();

        //设置为public
        field.setAccessible(true);

        try {
            switch (field.getGenericType().toString()){
                case "byte":
                    String b2str = String.valueOf(field.getByte(t));
                    if (!CheckUtil.isEmpty(b2str))
                        values.put(field.getName(), b2str);
                    break;

                case "short":
                    String s2str = String.valueOf(field.getShort(t));
                    if (!CheckUtil.isEmpty(s2str))
                        values.put(field.getName(), s2str);
                    break;

                case "long":
                    String l2str = String.valueOf(field.getLong(t));
                    if (!CheckUtil.isEmpty(l2str))
                        values.put(field.getName(), l2str);
                    break;

                case "float":
                    String f2str = String.valueOf(field.getFloat(t));
                    if (!CheckUtil.isEmpty(f2str))
                        values.put(field.getName(),f2str);
                    break;

                case "double":
                    String d2str = String.valueOf(field.getDouble(t));
                    if (!CheckUtil.isEmpty(d2str))
                        values.put(field.getName(), d2str);
                    break;

                case "class java.lang.String":
                    String o2str = String.valueOf(field.get(t));
                    if (!CheckUtil.isEmpty(o2str))
                        values.put(field.getName(), o2str);
                    break;

                case "boolean":
                    if (field.getBoolean(t)) {
                        values.put(field.getName(), "1");
                    }else {
                        values.put(field.getName(), "0");
                    }
                    break;

                case "int":
                    values.put(field.getName(), field.getInt(t));
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return values;
    }

    /**
     * 通过反射获取数据对应的数据值，
     * 并再新建model中设置对于变量的值
     *
     * @param field
     * @param cursor    数据库读取到当前数据(一条数据，即一个自定义 model 的数据)
     * @param cls
     * @param obj   通过映射新建的数据对象,用来保存数据库读取的数据
     * @return
     */
    private Object getValue(Field field, Cursor cursor, Class<T> cls, Object obj){
        if (null == cursor)
            return obj;

        //设置为public
        field.setAccessible(true);

        try {
            String name = field.getName();  //获取变量名和者数据库列名
            Field fieldSet = cls.getDeclaredField(name); //获取对象

            //设置为public
            fieldSet.setAccessible(true);

            switch (field.getGenericType().toString()){
                case "byte":
                    fieldSet.setByte(obj, Byte.parseByte(cursor.getString(cursor.getColumnIndex(name))));
                    break;

                case "short":
                    fieldSet.setShort(obj, Short.parseShort(cursor.getString(cursor.getColumnIndex(name))));
                    break;

                case "long":
                    fieldSet.setLong(obj, Long.parseLong(cursor.getString(cursor.getColumnIndex(name))));
                    break;

                case "float":
                    fieldSet.setFloat(obj, Float.parseFloat(cursor.getString(cursor.getColumnIndex(name))));
                    break;

                case "double":
                    fieldSet.setDouble(obj, Double.parseDouble(cursor.getString(cursor.getColumnIndex(name))));
                    break;

                case "class java.lang.String":
                    fieldSet.set(obj, cursor.getString(cursor.getColumnIndex(name)));
                    break;

                case "boolean":
                    fieldSet.setBoolean(obj, cursor.getString(cursor.getColumnIndex(name)).equals("1"));
                    break;

                case "int":
                    fieldSet.setInt(obj, cursor.getInt(cursor.getColumnIndex(name)));
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * 是否为合法Sql语句
     */
    private boolean isLegalSql(String sql) {
        if (sql != null && sql.length() > 1) {
            if ("(".equals(sql.charAt(0) + "") && ")".equals(sql.charAt(sql.length() - 1) + "")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加主键
     */
    private String addPrimaryKey() {
        return "(_id INTEGER PRIMARY KEY AUTOINCREMENT,";
    }

    /**
     * 创建TEXT型字段
     *
     * @param key 字段名
     */
    private String addText(String key) {
        return key + " TEXT,";
    }

    /**
     * 创建BLOB型字段
     *
     * @param key 字段名
     */
    private String addBlob(String key) {
        return key + " BLOB,";
    }

    /**
     * 创建INTEGER型字段
     *
     * @param key 字段名
     */
    private String addInteger(String key) {
        return key + " INTEGER,";
    }

    /**
     * 关闭全部
     */
    public void closeAll(Cursor mCursor, SQLiteOpenHelper mSQLiteOpenHelper, SQLiteDatabase mSQLiteDatabase) {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        } else {
            Log.v(TAG, "closeAll: mCursor已关闭");
        }
        if (mSQLiteOpenHelper != null) {
            mSQLiteOpenHelper.close();
        } else {
            Log.v(TAG, "closeAll: mSQLiteOpenHelper已关闭");
        }
        if (mSQLiteDatabase != null && mSQLiteDatabase.isOpen()) {
            mSQLiteDatabase.close();
        } else {
            Log.v(TAG, "closeAll: mSQLiteDatabase已关闭");
        }
    }

}
