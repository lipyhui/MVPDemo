package com.kawakp.kp.oxygenerator.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.kawakp.kp.oxygenerator.db.OxDBManager;
import com.kawakp.kp.oxygenerator.db.model.Warn;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by penghui.li on 2017/6/7.
 */

public class oxygeneratorApplication extends Application {
    private static oxygeneratorApplication mContext;

    //未解除的报警信息类型
    private static List<Integer> warnType = new ArrayList<>();
    //未解除的报警信息
    private static List<Warn> warnData = new ArrayList<>();

    //Activity 管理
    private static List<Activity> sActivityList = new ArrayList<>();

    @Override
    public void onCreate()
    {
        super.onCreate();

        mContext = this;

        //异常捕获初始化
        CrashHandler.getInstance().initCrashHandler(this); // 一定要先初始化
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler.getInstance());

        //AutoLayoutConifg.getInstance().useDeviceSize();//使用设备物理高度
        OxDBManager.createWarnTable(mContext);

        getWarnData();
    }

    /**
     * 获取context
     *
     * @return
     */
    public static Context getContext(){
        return mContext;
    }

    /**
     * 添加 Activity 到管理列表
     *
     * @param activity
     */
    public static void addActivity(Activity activity) {
        sActivityList.add(activity);
    }

    /**
     * 清除 Activity 到管理列表(销毁所有 Activity)
     */
    public static void clearActivity() {
        if (sActivityList != null) {
            for (Activity activity : sActivityList) {
                activity.finish();
            }

            sActivityList.clear();
        }
    }

    /**
     * 获取所有未解除的报警消息
     */
    private void getWarnData(){
        OxDBManager.getWarnDispelTime(mContext)
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<List<Warn>>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull List<Warn> warns) {
                        if (null == warnData)
                            warnData = new ArrayList<Warn>();

                        warnData.clear();

                        for (Warn warn:warns) {
                            warnData.add(warn);
                            warnType.add(warn.getType());
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
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
     * 只检查 0、1、2 位置的报警信息
     *
     * @param booleen
     */
    public static void checkWarn(Boolean[] booleen){
        if (booleen[0]){
            addWarn2DB(1);
        }else {
            dispelWarn(1);
        }

        if (booleen[1]){
            addWarn2DB(2);
        }else {
            dispelWarn(2);
        }

        if (booleen[2]){
            addWarn2DB(3);
        }else {
            dispelWarn(3);
        }
    }

    /**
     * 添加报警数据到数据库
     *
     * @param type  支持类型 1、2、3
     * @return  只有成功添加到数据库才会返回 Warn，失败则返回 null
     */
    public static Warn addWarn2DB(int type){

        if (type > 3 || type < 1)
            return null;

        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String time = format.format(new Date(System.currentTimeMillis()));

        if (!warnType.contains(type)) {
            OxDBManager.addWarnData(mContext, time, type, "0");
            Warn warn = new Warn(time, type, "0");
            warnData.add(warn);
            warnType.add(type);
            return warn;
        }

        return null;
    }


    /**
     * 消除报警
     *
     * @param type  支持类型 1、2、3
     * @return  返回事件不为空成功删除报警数据，返回时间为空未删除报警数据
     */
    public static String dispelWarn(int type){

        if (!warnType.contains(type))
            return null;

        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String time = format.format(new Date(System.currentTimeMillis()));

        OxDBManager.dispelWarn(mContext, type, time);
        List<Integer> copyWarnType = new ArrayList<>();
        copyWarnType.addAll(warnType);

        for (int i = (copyWarnType.size() - 1); i >= 0 ; i--){
            if (warnType.get(i) == type){
                warnType.remove(i);
                warnData.remove(i);
                return time;
            }
        }
        return null;
    }

    /**
     * 获取未处理的报警信息
     *
     * @return
     */
    public static List<Warn> getWarn(){
        return warnData;
    }

}
