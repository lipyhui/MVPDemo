package com.kawakp.kp.oxygenerator.view.fragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.app.oxygeneratorApplication;
import com.kawakp.kp.oxygenerator.base.BaseFragment;
import com.kawakp.kp.oxygenerator.constant.PageConstant;
import com.kawakp.kp.oxygenerator.constant.UpdateConstant;
import com.kawakp.kp.oxygenerator.interf.FragmentListener;
import com.kawakp.kp.oxygenerator.interf.WarnInfoListener;
import com.kawakp.kp.oxygenerator.view.dialog.ResetPasswordDialog;
import com.kawakp.kp.oxygenerator.view.fragment.set.ControlFragment;
import com.kawakp.kp.oxygenerator.view.fragment.set.ParamFragment;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link SetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetFragment extends BaseFragment implements WarnInfoListener {

    //@BindView(R2.id.sys_set)
    //View sysSet;

    //@BindView(R2.id.set_plc_time)
    TextView plcTime;

    //参数设置
    //@BindView(R2.id.set_param)
    View setParam;
    //@BindView(R2.id.set_param_tv)
    TextView setParamTv;
    //@BindView(R2.id.set_param_iv)
    ImageView setParamIv;

    //控制设置
    //@BindView(R2.id.set_control)
    View setControl;
    //@BindView(R2.id.set_control_tv)
    TextView setControlTv;
    //@BindView(R2.id.set_control_iv)
    ImageView setControlIv;

    //消息
    //@BindView(R2.id.set_info)
    ImageView info;

    //重置密码
    TextView resetPassword;

    //当前显示的fragment
    private BaseFragment currentFragment;

    private  ParamFragment paramFragment;
    private  ControlFragment controlFragment;

    private ResetPasswordDialog.Builder resetPasswordDialog;

    private int type = 0;

    //private int count = 0;

    //系统设置点击计时次数
//    private Disposable disSysClick;

    //时间订阅操作量
    private Disposable disposable;

    private FragmentListener listener;

    public SetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetFragment newInstance() {
        SetFragment fragment = new SetFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_set;
    }

    @Override
    public void initView(View view) {
        //sysSet = view.findViewById(R.id.sys_set);
        plcTime = (TextView) view.findViewById(R.id.set_plc_time);
        setParam = view.findViewById(R.id.set_param);
        setParamTv = (TextView) view.findViewById(R.id.set_param_tv);
        setParamIv = (ImageView) view.findViewById(R.id.set_param_iv);
        setControl = view.findViewById(R.id.set_control);
        setControlTv = (TextView) view.findViewById(R.id.set_control_tv);
        setControlIv = (ImageView) view.findViewById(R.id.set_control_iv);
        info = (ImageView) view.findViewById(R.id.set_info);
        resetPassword = (TextView) view.findViewById(R.id.set_reset_password);

        createAllFragment();

        setParam.setOnClickListener(this);
        setControl.setOnClickListener(this);
        info.setOnClickListener(this);
        resetPassword.setOnClickListener(this);

        //sysSet.setOnClickListener(this);

      /*  sysSet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.e("Test_touch", "ACTION_DOWN");
                        break;

                    case MotionEvent.ACTION_MOVE:
                        Log.e("Test_touch", "ACTION_MOVE");
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.e("Test_touch", "ACTION_UP");
                        break;
                }
                return true;
            }
        });*/
    }

    @Override
    public void initData() {
       /* if (null != currentFragment)
            currentFragment.initData();*/
        if (listener.getCurrentSet() == PageConstant.PARAM) {
            showSetParam();
        }else {
            showSetControl();
        }

        updateTime();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (null != currentFragment)
            currentFragment.onHiddenChanged(hidden);
    }

    @Override
    protected void hideFragment() {
        //polling = false;
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();

        if (resetPasswordDialog != null && resetPasswordDialog.isShowing()){
            resetPasswordDialog.cancel();
        }
    }

    /**
     * 获取时间
     */
    private void updateTime(){
        API.getTime()
                .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Object> objectObservable) throws Exception {
                        return objectObservable.delay(UpdateConstant.UPDATE_TIME, TimeUnit.SECONDS);
                    }
                })
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable)
                            throws Exception {
                        return throwableObservable.delay(UpdateConstant.UPDATE_TIME, TimeUnit.SECONDS);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        plcTime.setText(s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 创建所有需要fragment
     */
    private void createAllFragment(){
        paramFragment = ParamFragment.newInstance();
        controlFragment = ControlFragment.newInstance();

        paramFragment.setWarnListener(this);
        controlFragment.setWarnListener(this);

       /* FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        //参数设置
        transaction.add(R.id.set_fragment, paramFragment);
        transaction.hide(paramFragment);

        //控制设置
        transaction.add(R.id.set_fragment, controlFragment);
        transaction.hide(controlFragment);

        transaction.commit();*/
    }

    /**
     * 显示参数设置界面
     */
    private void showSetParam(){
        setParamIv.setVisibility(View.VISIBLE);
        setControlIv.setVisibility(View.INVISIBLE);

        setParamTv.setTextColor(ContextCompat.getColor(oxygeneratorApplication.getContext(), R.color.colorHomeDate));
        setControlTv.setTextColor(ContextCompat.getColor(oxygeneratorApplication.getContext(), R.color.colorHomeTitle));

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (currentFragment != null)
            transaction.hide(currentFragment);
        if(paramFragment.isAdded()){
            transaction.show(paramFragment);
        }else {
            transaction.add(R.id.set_fragment, paramFragment);
        }
        transaction.commit();

        currentFragment = paramFragment;

        type = 0;
    }

    /**
     * 显示控制设置界面
     */
    private void showSetControl(){
        setControlIv.setVisibility(View.VISIBLE);
        setParamIv.setVisibility(View.INVISIBLE);

        setControlTv.setTextColor(ContextCompat.getColor(oxygeneratorApplication.getContext(), R.color.colorHomeDate));
        setParamTv.setTextColor(ContextCompat.getColor(oxygeneratorApplication.getContext(), R.color.colorHomeTitle));

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (currentFragment != null)
            transaction.hide(currentFragment);
        if(controlFragment.isAdded()){
            transaction.show(controlFragment);
        }else {
            transaction.add(R.id.set_fragment, controlFragment);
        }
        transaction.commit();

        currentFragment = controlFragment;

        type = 1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_param:
                if (type == 0)
                    return;
                showSetParam();
                listener.currentSet(PageConstant.PARAM);
                break;

            case R.id.set_control:
                if (type == 1)
                    return;
                listener.currentSet(PageConstant.CONTROL);
                showSetControl();
                break;

            case R.id.set_info:
                if (listener != null)
                    listener.onInfo();
                break;

            case R.id.set_reset_password:
                resetPasswordDialog = new ResetPasswordDialog.Builder(getActivity());
                resetPasswordDialog.create().show();
                break;

            default:
                break;
        }
    }

    /**
     * 设置监听
     * @param listener
     */
    public void setFragmentListener(FragmentListener listener){
        this.listener = listener;
    }

    @Override
    public Activity getSuperActivity() {
        if (null != listener){
            return listener.getActivity();
        } else {
            return getActivity();
        }
    }

    @Override
    public void onWarning(boolean b) {
        if (b){
            info.setImageResource(R.drawable.icon_info_red);
        }else {
            info.setImageResource(R.drawable.icon_info);
        }
    }
}
