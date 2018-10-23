package com.kawakp.kp.oxygenerator.db;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kawakp.kp.oxygenerator.db.model.Warn;
import com.kawakp.kp.oxygenerator.util.CheckUtil;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 注意：
 * 所有返回 Observable 对象的方法都需要在
 * 订阅处理完后，取消订阅，不然可能导致内存泄漏
 *
 * Created by penghui.li on 2017/6/24.
 */
public class DBManager<T extends DBaseModel> {
    private String TAG = this.getClass().getSimpleName();

    //默认数据库名称
    private String NAME = "kawakp";

    //默认数据库版本号
    private int VERSION = 1;

    //数据库一个表保存的数据条数
    private int SAVE_NUMBER = 1000;

    private DBHelper dbHelper;
    private SqlOperation sqlOperation;

    DBManager(Context context, String name, int version){

        if (null == context){
            Log.e(TAG, "context is null!");
            return;
        }

        if (!CheckUtil.isEmpty(name))
            NAME = name;

        if (version > 1)
            VERSION = version;

        //创建数据库
        dbHelper = new DBHelper(context, NAME +".db", VERSION);
        sqlOperation = new SqlOperation();
    }

    /**
     * 创建表格(每次只创建一个表格)
     *
     * @param classes
     */
    public void createTable(Class<T>... classes){
        Observable.fromArray(classes)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<Class<T>>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Class<T> tClass) {
                        sqlOperation.createTable(dbHelper, tClass);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }
                });
    }

    /**
     * 存储一条数据到数据库
     * (需要保证数据库已经创建对于的数据表，且数据不能为空)
     *
     * @param data
     * @return  true:succeed    false:failed
     */
    public Observable<Boolean> addData(final T data){
        return Observable.just(data)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<T>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull T t) throws Exception {
                        int count = sqlOperation.getCount(dbHelper, t.getClass());
                        if (count >= SAVE_NUMBER && null != t)
                            sqlOperation.delete(dbHelper, t.getClass(), "_id in(select _id from Warn order by _id asc limit 0," + (count - SAVE_NUMBER + 1) + ")", null);
                    }
                })
                .map(new Function<T, Boolean>() {
                    @Override
                    public Boolean apply(@io.reactivex.annotations.NonNull T t) throws Exception {
                        return sqlOperation.insert(dbHelper, t);
                    }
                });
    }

    /**
     * 存储一条数据到数据库，不关心是否存储成功
     * (需要保证数据库已经创建对于的数据表，且数据不能为空)
     *
     * @param data
     */
    public void  addDataWithoutReturn(T data){
        addData(data)
                .subscribe(new Observer<Boolean>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Boolean aBoolean) {

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }
                });
    }

    /**
     * 存储一组同类数据到数据库
     *(需要保证数据库已经创建对于的数据表，且数据不能为空)
     *
     * @param tList
     * @return  true:succeed    false:failed
     */
    public Observable<List<Boolean>> addDataList(List<T> tList){
        return Observable.just(tList)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<List<T>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull List<T> tList) throws Exception {
                        int count = sqlOperation.getCount(dbHelper, tList.get(0).getClass());
                        if (count >= 1000 && tList.size() > 0)
                            sqlOperation.delete(dbHelper, tList.get(0).getClass(), "_id in(select _id from Warn order by _id asc limit 0," + (count - SAVE_NUMBER + tList.size()) + ")", null);
                    }
                })
                .map(new Function<List<T>, List<Boolean>>() {
                    @Override
                    public List<Boolean> apply(@io.reactivex.annotations.NonNull List<T> tList) throws Exception {
                        return sqlOperation.insert(dbHelper, tList);
                    }
                });
    }

    /**
     * 存储一组同类数据到数据库,不关心是否存储成功
     *(需要保证数据库已经创建对于的数据表，且数据不能为空)
     *
     * @param tList
     */
    public void addDataListWithoutReturn(List<T> tList){
        addDataList(tList)
                .subscribe(new Observer<List<Boolean>>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull List<Boolean> booleen) {

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }
                });
    }

    /**
     * 根据条件替换数据
     *(需要保证数据库已经创建对于的数据表，且数据不能为空)
     *
     * 列如:
     *  ContentValues values = new ContentValues();
     *  values.put("time", "time a");   //把表的 time 列的数据替换为 "time a"
     *
     *  String whereClause = "time = ?";        //匹配条件
     *  String[] whereArgs = {"string"};      //匹配满足条件
     *
     *  update(Warn.class, values, whereClause, whereArgs); //替换 Warn 表里 time 列所有 "string" 替换为 "time a"
     *
     * @param cls   表名
     * @param values    要替换的对应列的值
     * @param whereClause   替换匹配条件
     * @param whereArgs     替换满足条件
     * @return
     */
    public Observable<Integer> updateData(final Class<T> cls, final ContentValues values, final String whereClause, final String[] whereArgs){
        return Observable.just(cls)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .map(new Function<Class<T>, Integer>() {
                    @Override
                    public Integer apply(@io.reactivex.annotations.NonNull Class<T> tClass) throws Exception {
                        return sqlOperation.update(dbHelper, cls, values, whereClause, whereArgs);
                    }
                });
    }

    /**
     * 根据条件替换数据,不关心是否更新成功
     *(需要保证数据库已经创建对于的数据表，且数据不能为空)
     *
     * @param cls
     * @param values
     * @param whereClause
     * @param whereArgs
     */
    public void updateDateWithoutReturn(final Class<T> cls, final ContentValues values, final String whereClause, final String[] whereArgs){
        updateData(cls, values, whereClause, whereArgs)
                .subscribe(new Observer<Integer>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Integer integer) {

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }
                });
    }

    /**
     * 根据条件删除数据
     *(需要保证数据库已经创建对于的数据表)
     *
     * @param cls
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public Observable<Boolean> deleteData(final Class<T> cls, final String whereClause, final String[] whereArgs){
        return Observable.just(cls)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .map(new Function<Class<T>, Boolean>() {
                    @Override
                    public Boolean apply(@io.reactivex.annotations.NonNull Class<T> tClass) throws Exception {
                        return sqlOperation.delete(dbHelper, cls, whereClause, whereArgs);
                    }
                });
    }

    /**
     * 根据条件删除数据,不关心是否删除成功
     *(需要保证数据库已经创建对于的数据表)
     *
     * @param cls
     * @param whereClause
     * @param whereArgs
     */
    public void  deleteDataWithoutReturn(final Class<T> cls, final String whereClause, final String[] whereArgs){
        deleteData(cls, whereClause, whereArgs)
                .subscribe(new Observer<Boolean>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Boolean aBoolean) {

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }
                });
    }

    /**
     * 清除一个表里的所有数据
     *
     * @param cls
     * @return
     */
    public Observable<Boolean> clearTable(final Class<T> cls){
        return Observable.just(cls)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .map(new Function<Class<T>, Boolean>() {
                    @Override
                    public Boolean apply(@io.reactivex.annotations.NonNull Class<T> tClass) throws Exception {
                        return sqlOperation.deleteAll(dbHelper, cls);
                    }
                });
    }

    /**
     * 清除一个表里的所有数据,不关心是否清除成功
     *
     * @param cls
     */
    public void clearTableWithoutReturn(final Class<T> cls){
        clearTable(cls)
                .subscribe(new Observer<Boolean>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Boolean aBoolean) {

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
                    }
                });
    }

    /**
     * 根据条件查询数据，不排序
     *
     * @param cls
     * @param selection
     * @param selectionArgs
     * @return
     */
    public Observable<List<T>> getDataSelectionWithoutOrder(final Class<T> cls, final String selection, final String[] selectionArgs){
        return Observable.just(cls)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .map(new Function<Class<T>, List<T>>() {
                    @Override
                    public List<T> apply(@io.reactivex.annotations.NonNull Class<T> tClass) throws Exception {
                        return sqlOperation.query(dbHelper, cls, null, selection, selectionArgs, null, null);
                    }
                });
    }

    /**
     * 查询所有数据，不排序
     *
     * @param cls
     * @return
     */
    public Observable<List<T>> getDataWithoutOrder(final Class<T> cls){
        return Observable.just(cls)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .map(new Function<Class<T>, List<T>>() {
                    @Override
                    public List<T> apply(@io.reactivex.annotations.NonNull Class<T> tClass) throws Exception {
                        return sqlOperation.fetchAll(dbHelper, cls, null, null);
                    }
                });
    }

    /**
     * 查询所有数据，并排序
     *
     * @param cls
     * @param by
     * @param order
     * @return
     */
    public Observable<List<T>> getData(final Class<T> cls, final String by, final SqlOperation.ORDER order){
        return Observable.just(cls)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .map(new Function<Class<T>, List<T>>() {
                    @Override
                    public List<T> apply(@io.reactivex.annotations.NonNull Class<T> tClass) throws Exception {
                        return sqlOperation.fetchAll(dbHelper, cls, by, order);
                    }
                });
    }

    public static final class Builder<T extends DBaseModel> {

        private Context context;

        //默认数据库名称
        private String NAME = "kawakp";

        //默认数据库版本号
        private int VERSION = 1;

        //数据库一个表保存的数据条数
        private int SAVE_NUMBER = 1000;

        public Builder(Context context){
            this.context = context;
        }

        /**
         * 设置上下文(该方法必须调用)
         *
         * @return
         */
   /*     public Builder setContext(Context context){
            this.context = context;
            return this;
        }*/

        /**
         * 设置数据库名
         *
         * @return
         */
        public Builder setDBName(String name){
            NAME = name;
            return this;
        }

        /**
         * 设置数据库版本号
         *
         * @param version
         * @return
         */
        public Builder setVERSION(int version) {
            VERSION = version;
            return this;
        }

        /**
         * 设置数据库最多保存的条数
         *
         * @return
         */
        public Builder setMaxNum(int maxNum){
            SAVE_NUMBER = maxNum;
            return this;
        }

        /**
         * 组建Snackbar,该方法在配置完所有项之后调用,必须调用
         * @return
         */
        public DBManager build() {
            return new DBManager(context, NAME, VERSION);
        }
    }

}
