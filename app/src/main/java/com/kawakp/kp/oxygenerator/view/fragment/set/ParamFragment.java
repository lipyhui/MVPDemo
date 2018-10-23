package com.kawakp.kp.oxygenerator.view.fragment.set;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.app.oxygeneratorApplication;
import com.kawakp.kp.oxygenerator.base.BaseFragment;
import com.kawakp.kp.oxygenerator.constant.UpdateConstant;
import com.kawakp.kp.oxygenerator.interf.WarnInfoListener;
import com.kawakp.kp.oxygenerator.util.StringDecimalUtil;
import com.kawakp.kp.oxygenerator.view.SetParamPopupWindow;
import com.kawakp.kp.oxygenerator.view.dialog.WaitSetDialog;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link ParamFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParamFragment extends BaseFragment {

    //设置参数数据
    //@BindView(R2.id.set_p_tv)
    TextView setPTv;
    //@BindView(R2.id.set_time_tv)
    TextView setTimeTv;
    //@BindView(R2.id.set_oo_tv)
    TextView setOOTv;

    //设置参数按钮
    //@BindView(R2.id.set_p)
    View setP;
    //@BindView(R2.id.set_time)
    View setTime;
    //@BindView(R2.id.set_oo)
    View setOO;

    //运行参数
    //@BindView(R2.id.set_run_time)
    TextView runTime;
    //@BindView(R2.id.set_run_con)
    TextView runCon;
    //@BindView(R2.id.set_run_p)
    TextView runP;
    //@BindView(R2.id.set_run_h)
    TextView runH;
    //@BindView(R2.id.set_run_t)
    TextView runT;

    //室内压力设定值
    private String sP = "0";
    //运行时间设定值
    private String sTime = "0";
    //氧气浓度设定值
    private String sOOCon = "0";

    private SetParamPopupWindow pPopupWindow;
    private SetParamPopupWindow timePopupWindow;
    private SetParamPopupWindow ooPopupWindow;

    //用于切换界面和推出时取消订阅
    private CompositeDisposable composite = new CompositeDisposable();

    //判断 Fragment 是否处于隐藏状态
    //private boolean polling = true;

    private WarnInfoListener warnListener;

    public ParamFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ParamFragment newInstance() {
        ParamFragment fragment = new ParamFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_set_param;
    }

    @Override
    public void initView(View view) {
        setPTv = (TextView) view.findViewById(R.id.set_p_tv);
        setTimeTv = (TextView) view.findViewById(R.id.set_time_tv);
        setOOTv = (TextView) view.findViewById(R.id.set_oo_tv);
        setP = view.findViewById(R.id.set_p);
        setTime = view.findViewById(R.id.set_time);
        setOO = view.findViewById(R.id.set_oo);
        runTime = (TextView) view.findViewById(R.id.set_run_time);
        runCon = (TextView) view.findViewById(R.id.set_run_con);
        runP = (TextView) view.findViewById(R.id.set_run_p);
        runH = (TextView) view.findViewById(R.id.set_run_h);
        runT = (TextView) view.findViewById(R.id.set_run_t);

        setP.setOnClickListener(this);
        setTime.setOnClickListener(this);
        setOO.setOnClickListener(this);
    }

    @Override
    public void initData() {
        updateInfo();
        updateRunParam();
        updateSetParam();
    }

    @Override
    protected void hideFragment() {
        //polling = false;
        composite.clear();

        if (pPopupWindow != null && pPopupWindow.isShowing()){
            pPopupWindow.dismiss();
        }
        if (timePopupWindow != null && timePopupWindow.isShowing()) {
            timePopupWindow.dismiss();
        }
        if (ooPopupWindow != null && ooPopupWindow.isShowing()) {
            ooPopupWindow.dismiss();
        }
    }

    /**
     * 监听报警消息
     */
    private void updateInfo(){
        composite.add(API.getWarnStatus()
                .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Object> objectObservable) throws Exception {
                        return objectObservable.delay(UpdateConstant.UPDATE_DATA, TimeUnit.SECONDS);
                    }
                })
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable)
                            throws Exception {
                        return throwableObservable.delay(UpdateConstant.UPDATE_DATA, TimeUnit.SECONDS);
                    }
                })
                .map(new Function<Boolean[], Boolean[]>() {
                    @Override
                    public Boolean[] apply(@NonNull Boolean[] booleen) throws Exception {
                        oxygeneratorApplication.checkWarn(booleen);
                        return booleen;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean[]>() {
                    @Override
                    public void accept(@NonNull Boolean[] booleen) throws Exception {
                        if (null != warnListener)
                            warnListener.onWarning(booleen[0] || booleen[1] || booleen[2]);
                    }
                }));
    }

    /**
     * 更新设置参数
     */
    private void updateSetParam(){
        composite.add(API.getSetData()
                .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Object> objectObservable) throws Exception {
                        return objectObservable.delay(UpdateConstant.UPDATE_DATA, TimeUnit.SECONDS);
                    }
                })
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable)
                            throws Exception {
                        return throwableObservable.delay(UpdateConstant.UPDATE_DATA, TimeUnit.SECONDS);
                    }
                })
                .map(new Function<String[], String[]>() {
                    @Override
                    public String[] apply(@NonNull String[] strings) throws Exception {
                        sP = strings[0];
                        sTime = strings[1];
                        sOOCon = strings[2];

                        strings[0] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(sP)/10f), 1) + " Kpa";
                        strings[1] = sTime + " min";
                        strings[2] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(sOOCon)/100f), 2) + " %";
                        return strings;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String[]>() {
                    @Override
                    public void accept(@NonNull String[] strings) throws Exception {
                        setPTv.setText(strings[0]);
                        setTimeTv.setText(strings[1]);
                        setOOTv.setText(strings[2]);
                    }
                }));
    }

    /**
     * 更新运行参数(运行时间、氧气浓度、室内压力、室内湿度、室内温度)
     */
    private void updateRunParam(){
        composite.add(API.getSetRunData()
                .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Object> objectObservable) throws Exception {
                        return objectObservable.delay(UpdateConstant.UPDATE_DATA, TimeUnit.SECONDS);
                    }
                })
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable)
                            throws Exception {
                        return throwableObservable.delay(UpdateConstant.UPDATE_DATA, TimeUnit.SECONDS);
                    }
                })
                .map(new Function<String[], String[]>() {
                    @Override
                    public String[] apply(@NonNull String[] strings) throws Exception {
                        strings[0] += " min";
                        strings[1] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[1])/100f), 2) + " %";
                        strings[2] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[2])/10f), 1) + " Kpa";
                        strings[3] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[3])/10f), 1) + " %";
                        strings[4] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[4])/10f), 1) + " ℃";
                        return strings;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String[]>() {
                    @Override
                    public void accept(@NonNull String[] strings) throws Exception {
                        runTime.setText(strings[0]);
                        runCon.setText(strings[1]);
                        runP.setText(strings[2]);
                        runH.setText(strings[3]);
                        runT.setText(strings[4]);
                    }
                }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_p: {
                pPopupWindow = new SetParamPopupWindow(
                        null != warnListener ? warnListener.getSuperActivity():getActivity(), SetParamPopupWindow.Type.ALL);
                pPopupWindow.setCyclic(true);
                pPopupWindow.setRange(12, 18, 0, 9, 1, 0);
                // 参数选择后回调
                pPopupWindow.setOnParamSelectListener(new SetParamPopupWindow.OnParamSelectListener() {
                    @Override
                    public void onParamSelect(final String data) {
                        final WaitSetDialog.Builder wait = new WaitSetDialog.Builder(getActivity());
                        wait.create().show();

                        API.setPValue((int) (Float.parseFloat(data)*10))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Boolean>() {
                                    private Disposable mDisposable;
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {
                                        mDisposable = d;
                                    }

                                    @Override
                                    public void onNext(@NonNull Boolean aBoolean) {
                                        if (aBoolean) {
                                            setPTv.setText(data + " Kpa");

                                            wait.succeed();
                                        } else {
                                            wait.error();
                                        }

                                        if (null != mDisposable && !mDisposable.isDisposed()){
                                            mDisposable.dispose();
                                        }
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {
                                        wait.error();

                                        if (null != mDisposable && !mDisposable.isDisposed()){
                                            mDisposable.dispose();
                                        }
                                    }

                                    @Override
                                    public void onComplete() {
                                        if (null != mDisposable && !mDisposable.isDisposed()){
                                            mDisposable.dispose();
                                        }
                                    }
                                });
                    }
                });
                pPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            }

            case R.id.set_time: {
                timePopupWindow = new SetParamPopupWindow(
                        null != warnListener ? warnListener.getSuperActivity():getActivity(), SetParamPopupWindow.Type.INTEGER);
                timePopupWindow.setCyclic(true);
                timePopupWindow.setIntegerRange(0, 120);
                // 参数选择后回调
                timePopupWindow.setOnParamSelectListener(new SetParamPopupWindow.OnParamSelectListener() {
                    @Override
                    public void onParamSelect(final String data) {
                        final WaitSetDialog.Builder wait = new WaitSetDialog.Builder(getActivity());
                        wait.create().show();

                        API.setTimeValue(Integer.parseInt(data))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Boolean>() {
                                    private Disposable mDisposable;
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {
                                        mDisposable = d;
                                    }

                                    @Override
                                    public void onNext(@NonNull Boolean aBoolean) {
                                        if (aBoolean) {
                                            setTimeTv.setText(data + " min");

                                            wait.succeed();
                                        } else {
                                            wait.error();
                                        }

                                        if (null != mDisposable && !mDisposable.isDisposed()){
                                            mDisposable.dispose();
                                        }
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {
                                        wait.error();

                                        if (null != mDisposable && !mDisposable.isDisposed()){
                                            mDisposable.dispose();
                                        }
                                    }

                                    @Override
                                    public void onComplete() {
                                        if (null != mDisposable && !mDisposable.isDisposed()){
                                            mDisposable.dispose();
                                        }
                                    }
                                });
                    }
                });
                timePopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            }

            case R.id.set_oo: {
                ooPopupWindow = new SetParamPopupWindow(
                        null != warnListener ? warnListener.getSuperActivity():getActivity(), SetParamPopupWindow.Type.ALL);
                ooPopupWindow.setCyclic(true);
                ooPopupWindow.setRange(21, 30, 0, 99, 1, 0);
                // 参数选择后回调
                ooPopupWindow.setOnParamSelectListener(new SetParamPopupWindow.OnParamSelectListener() {
                    @Override
                    public void onParamSelect(final String data) {
                        final WaitSetDialog.Builder wait = new WaitSetDialog.Builder(getActivity());
                        wait.create().show();

                        API.setConValue((int) (Float.parseFloat(data)*100))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Boolean>() {
                                    private Disposable mDisposable;
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {
                                        mDisposable = d;
                                    }

                                    @Override
                                    public void onNext(@NonNull Boolean aBoolean) {
                                        if (aBoolean) {
                                            setOOTv.setText(data + " %");

                                            wait.succeed();
                                        } else {
                                            wait.error();
                                        }

                                        if (null != mDisposable && !mDisposable.isDisposed()){
                                            mDisposable.dispose();
                                        }
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {
                                        wait.error();

                                        if (null != mDisposable && !mDisposable.isDisposed()){
                                            mDisposable.dispose();
                                        }
                                    }

                                    @Override
                                    public void onComplete() {
                                        if (null != mDisposable && !mDisposable.isDisposed()){
                                            mDisposable.dispose();
                                        }
                                    }
                                });
                    }
                });
                ooPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            }
        }
    }

    /**
     * 设置警告消息监听
     * @param listener
     */
    public void setWarnListener(WarnInfoListener listener){
        this.warnListener = listener;
    }
}
