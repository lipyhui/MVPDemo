package com.kawakp.kp.oxygenerator.view.fragment;

import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.app.oxygeneratorApplication;
import com.kawakp.kp.oxygenerator.base.BaseFragment;
import com.kawakp.kp.oxygenerator.constant.UpdateConstant;
import com.kawakp.kp.oxygenerator.interf.FragmentListener;
import com.kawakp.kp.oxygenerator.util.StringDecimalUtil;
import com.kawakp.kp.oxygenerator.view.dialog.WaitSetDialog;
import com.kawakp.kp.oxygenerator.view.password.PasswordSeeActivity;

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
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends BaseFragment {

    View sysHome;

    //@BindView(R2.id.home_plc_time)
    TextView plcTime;

    //消息
    //@BindView(R2.id.home_info)
    ImageView info;

    //@BindView(R2.id.inner_ring)
    View innerRing;     //内环
    //@BindView(R2.id.outer_ring)
    View outerRing;     //外环

    //氧气浓度报警图标
    //@BindView(R2.id.home_oo_status)
/*    ImageView ooStatus;
    //湿度报警图标
    //@BindView(R2.id.home_t_status)
    ImageView tStatus;
    //压强报警图标
    //@BindView(R2.id.home_p_status)
    ImageView pStatus;*/

    View warnBg;
    View warnLayout;
    TextView warnTv;

    //含氧量
    //@BindView(R2.id.home_oo_con)
    TextView ooCon;
    //温度
    //@BindView(R2.id.home_t)
    TextView T;
    //湿度
    //@BindView(R2.id.home_h)
    TextView H;
    //压力
    //@BindView(R2.id.home_p)
    TextView P;
    //已运行时间
    //@BindView(R2.id.home_ran_time)
    TextView ranTime;
    //总运行时间
    //@BindView(R2.id.home_all_run_time)
    TextView allRunTime;

    //加压
    //@BindView(R2.id.home_p_add)
    ImageView addP;
    //弥散氧
    //@BindView(R2.id.home_oo_dispersion)
    ImageView dispersionOO;
    //呼吸氧
    //@BindView(R2.id.home_oo_breath)
    ImageView breathOO;
    //卸压
    //@BindView(R2.id.home_p_del)
    ImageView delP;

    //灯按钮、图片、设备关闭图片按钮
    //@BindView(R2.id.lamp)
    ImageView lamp;
    //@BindView(R2.id.lamp_iv)
    ImageView lampIv;
    //@BindView(R2.id.stop_equipment)
    ImageView stopEquipment;

    //门按钮、图片
    //@BindView(R2.id.door)
    ImageView door;
    //@BindView(R2.id.door_iv)
    ImageView doorIv;

    private boolean[] devicesStatus = {false, false, false, false};

	/** 设备是否处于运行状态 */
	private boolean deviceStatus = false;
    private boolean longClick =false;
    private Disposable longClickDisposable;

    //用于切换界面和推出时取消订阅
    private CompositeDisposable composite = new CompositeDisposable();
    //判断 Fragment 是否处于隐藏状态
    //private boolean polling = true;

    //报警闪动状态取消订阅
    private Disposable disWarning = null;

    private FragmentListener listener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void initView(View view) {
        sysHome = view.findViewById(R.id.sys_home);

        plcTime = (TextView) view.findViewById(R.id.home_plc_time);
        info = (ImageView) view.findViewById(R.id.home_info);
        innerRing = view.findViewById(R.id.inner_ring);
        outerRing = view.findViewById(R.id.outer_ring);
        outerRing = view.findViewById(R.id.outer_ring);
       /* ooStatus = (ImageView) view.findViewById(R.id.home_oo_status);
        tStatus = (ImageView) view.findViewById(R.id.home_t_status);
        pStatus = (ImageView) view.findViewById(R.id.home_p_status);*/

        warnBg = view.findViewById(R.id.home_warn_bg);
        warnLayout = view.findViewById(R.id.home_warn);
        warnTv = (TextView) view.findViewById(R.id.home_warn_tv);

        ooCon = (TextView) view.findViewById(R.id.home_oo_con);
        T = (TextView) view.findViewById(R.id.home_t);
        H = (TextView) view.findViewById(R.id.home_h);
        P = (TextView) view.findViewById(R.id.home_p);
        ranTime = (TextView) view.findViewById(R.id.home_ran_time);
        allRunTime = (TextView) view.findViewById(R.id.home_all_run_time);
        addP = (ImageView) view.findViewById(R.id.home_p_add);
        dispersionOO = (ImageView) view.findViewById(R.id.home_oo_dispersion);
        breathOO = (ImageView) view.findViewById(R.id.home_oo_breath);
        delP = (ImageView) view.findViewById(R.id.home_p_del);
        lamp = (ImageView) view.findViewById(R.id.lamp);
        lampIv = (ImageView) view.findViewById(R.id.lamp_iv);
        stopEquipment = (ImageView) view.findViewById(R.id.stop_equipment);
        door = (ImageView) view.findViewById(R.id.door);
        doorIv = (ImageView) view.findViewById(R.id.door_iv);

        sysHome.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!longClick){
                            longClick = true;
                            Observable.timer(7, TimeUnit.SECONDS)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<Long>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {
                                            longClickDisposable = d;
                                        }

                                        @Override
                                        public void onNext(@NonNull Long aLong) {
                                            if (longClick){
                                                Intent intent = new Intent(oxygeneratorApplication.getContext(), PasswordSeeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                oxygeneratorApplication.getContext().startActivity(intent);
                                            }
                                            if (null != longClickDisposable && !longClickDisposable.isDisposed()) {
                                                longClickDisposable.dispose();
                                                longClickDisposable = null;
                                                longClick = false;
                                            }
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {
                                            if (null != longClickDisposable && !longClickDisposable.isDisposed()) {
                                                longClickDisposable.dispose();
                                                longClickDisposable = null;
                                                longClick = false;
                                            }
                                        }

                                        @Override
                                        public void onComplete() {
                                            if (null != longClickDisposable && !longClickDisposable.isDisposed()) {
                                                longClickDisposable.dispose();
                                                longClickDisposable = null;
                                                longClick = false;
                                            }
                                        }
                                    });
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (null != longClickDisposable && !longClickDisposable.isDisposed()) {
                            longClickDisposable.dispose();
                            longClickDisposable = null;
                            longClick = false;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (null != longClickDisposable && !longClickDisposable.isDisposed()) {
                            longClickDisposable.dispose();
                            longClickDisposable = null;
                            longClick = false;
                        }
                        break;
                }
                return true;
            }
        });

        stopEquipment.setOnClickListener(this);
        lamp.setOnClickListener(this);
        door.setOnClickListener(this);
        info.setOnClickListener(this);
    }

    @Override
    public void initData() {

        updateTime();

        updateInfo();
        updateDeviceData();
        updateDeviceStatus();
        updateOther();
    }

    @Override
    protected void hideFragment() {
        //polling = false;

        if (null != disWarning && !disWarning.isDisposed()){
            disWarning.dispose();
            disWarning = null;
        }

        stopInnerRing();
        stopOuterRing();
        deviceStatus = false;

        composite.clear();
    }

    /**
     * 获取时间
     */
    private void updateTime(){
        composite.add(API.getTime()
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
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        plcTime.setText(s);
                    }
                }));
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
                       /* if (isHidden())
                            return;*/

                        StringBuffer buffer = new StringBuffer();
                        if (booleen[0]){
                            //pStatus.setImageResource(R.drawable.status_abnormal);
                            //buffer.append(oxygeneratorApplication.getContext().getResources().getString(R.string.warn_p_h));
                            buffer.append("压力高");
                            buffer.append("，");
                        }/*else {
                            //pStatus.setImageResource(R.drawable.status_normal);
                        }*/

                        if (booleen[1]){
                            //tStatus.setImageResource(R.drawable.status_abnormal);
                            //buffer.append(oxygeneratorApplication.getContext().getResources().getString(R.string.warn_con_h));
                            buffer.append("浓度高");
                            buffer.append("，");
                        }/*else {
                            //tStatus.setImageResource(R.drawable.status_normal);
                        }*/

                        if (booleen[2]){
                            //ooStatus.setImageResource(R.drawable.status_abnormal);
                            //buffer.append(oxygeneratorApplication.getContext().getResources().getString(R.string.warn_oo_l));
                            buffer.append("储氧量不足");
                            buffer.append("，");
                        }/*else {
                            //ooStatus.setImageResource(R.drawable.status_normal);
                        }*/

                        if (buffer.length() > 0)
                            warnTv.setText(buffer.deleteCharAt(buffer.length() - 1).toString());

                        if (booleen[0] || booleen[1] || booleen[2]){
                            if (null == disWarning || disWarning.isDisposed()) {
                                info.setImageResource(R.drawable.icon_info_red);
                                warnBg.setVisibility(View.VISIBLE);
                                warnLayout.setVisibility(View.VISIBLE);
                                warnFlashing();
                            }
                        }else {
                            if (null != disWarning && !disWarning.isDisposed()){
                                disWarning.dispose();
                                disWarning = null;
                            }

                            info.setImageResource(R.drawable.icon_info);
                            warnBg.setVisibility(View.INVISIBLE);
                            warnLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                }));
    }

    /**
     * 报警界面闪动
     */
    private void warnFlashing(){
        if (null != disWarning && !disWarning.isDisposed())
            return;

        disWarning = Observable.interval(500, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<Long>() {
            @Override
            public void accept(@NonNull Long aLong) throws Exception {
                Log.e("MainHome", "aLong = " + aLong);
                if (aLong % 2 == 0){
                    warnBg.setVisibility(View.INVISIBLE);
                }else {
                    warnBg.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 更新设备数据(含氧量、温度、湿度、压力、已运行时间、总运行时间)
     */
    private void updateDeviceData(){
        composite.add(API.getHomeData()
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
                      /*  strings[0] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[0])/100f), 2) + "%";
                        strings[1] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[1])/10f), 1) + "℃";
                        strings[2] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[2])/10f), 1) + "%";*/

                        strings[0] = String.valueOf(Integer.parseInt(strings[0])/100) + "%";
                        strings[1] = String.valueOf(Integer.parseInt(strings[1])/10) + "℃";
                        strings[2] = String.valueOf(Integer.parseInt(strings[2])/10) + "%";

                        strings[3] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[3])/10f), 1) + "Kpa";
                        strings[4] += "min";
                        strings[5] += "min";
                        return strings;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String[]>() {
                    @Override
                    public void accept(@NonNull String[] strings) throws Exception {
                        ooCon.setText(strings[0]);
                        T.setText(strings[1]);
                        H.setText(strings[2]);
                        P.setText(strings[3]);
                        ranTime.setText(strings[4]);
                        allRunTime.setText(strings[5]);
                    }
                }));
    }

    /**
     * 更新设备相关控制状态(加压、弥散氧、呼吸氧、卸压)
     */
    private void updateDeviceStatus(){
        composite.add(API.getHomeDeviceStatus()
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean[]>() {
                    @Override
                    public void accept(@NonNull Boolean[] booleen) throws Exception {
                        if (booleen[0]){
                            if (!devicesStatus[0]) {
                                devicesStatus[0] = true;
                                addP.setImageResource(R.drawable.icon_p_add_running);
                            }
                        }else {
                            if (devicesStatus[0]) {
                                devicesStatus[0] = false;
                                addP.setImageResource(R.drawable.icon_p_add_stop);
                            }
                        }

                        if (booleen[1]){
                            if (!devicesStatus[1]) {
                                devicesStatus[1] = true;
                                dispersionOO.setImageResource(R.drawable.icon_oo_dispersion_running);
                            }
                        }else {
                            if (devicesStatus[1]) {
                                devicesStatus[1] = false;
                                dispersionOO.setImageResource(R.drawable.icon_oo_dispersion_stop);
                            }
                        }

                        if (booleen[2]){
                            if (!devicesStatus[2]) {
                                devicesStatus[2] = true;
                                breathOO.setImageResource(R.drawable.icon_oo_breath_running);
                            }
                        }else {
                            if (devicesStatus[2]) {
                                devicesStatus[2] = false;
                                breathOO.setImageResource(R.drawable.icon_oo_breath_stop);
                            }
                        }

                        if (booleen[3]){
                            if (!devicesStatus[3]) {
                                devicesStatus[3] = true;
                                delP.setImageResource(R.drawable.icon_p_del_running);
                            }
                        }else {
                            if (devicesStatus[3]) {
                                devicesStatus[3] = false;
                                delP.setImageResource(R.drawable.icon_p_del_stop);
                            }
                        }
                    }
                }));
    }

    /**
     * 更新灯、门、设备开关的状态
     */
    private void updateOther(){
        composite.add(API.getHomeOtherStatus()
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean[]>() {
                    @Override
                    public void accept(@NonNull Boolean[] booleen) throws Exception {
                        if (booleen[0]){
                            if (!deviceStatus) {
                                stopEquipment.setImageResource(R.drawable.btn_switch_equipment_state_open);
                                deviceStatus = true;
                                startInnerRing();
                                startOuterRing();
                            }
                        } else {
                            if (deviceStatus) {
                                stopEquipment.setImageResource(R.drawable.btn_switch_equipment_state_close);
                                deviceStatus = false;
                                stopInnerRing();
                                stopOuterRing();
                            }
                        }

                        if (booleen[1]){
                            if ("false".equals(lamp.getTag())) {
                                lampIv.setImageResource(R.drawable.icon_lamp_running);
                                lamp.setImageResource(R.drawable.btn_switch_state_open);
                                lamp.setTag("true");
                            }
                        } else {
                            if ("true".equals(lamp.getTag())) {
                                lampIv.setImageResource(R.drawable.icon_lamp_stop);
                                lamp.setImageResource(R.drawable.btn_switch_state_close);
                                lamp.setTag("false");
                            }
                        }

                        if (booleen[2]){
                            if ("false".equals(door.getTag())) {
                                doorIv.setImageResource(R.drawable.icon_door_running);
                                door.setImageResource(R.drawable.btn_switch_state_open);
                                door.setTag("true");
                            }
                        } else {
                            if ("true".equals(door.getTag())) {
                                doorIv.setImageResource(R.drawable.icon_door_stop);
                                door.setImageResource(R.drawable.btn_switch_state_close);
                                door.setTag("false");
                            }
                        }
                    }
                }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.stop_equipment:
                /*if (listener != null)
                    listener.logout();
                RedirectUtil.redirectToForResult(oxygeneratorApplication.getContext(), LoginActivity.class, RedirectUtil.BundleKey.LOGIN);*/

                if (null != listener && listener.isLogin()){

                    final WaitSetDialog.Builder wait = new WaitSetDialog.Builder(getActivity());
                    wait.create().show();

					if (deviceStatus) {
                        API.setDeviceStatus(false)
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
                                            stopEquipment.setImageResource(R.drawable.btn_switch_equipment_state_close);
                                            deviceStatus = false;
                                            stopInnerRing();
                                            stopOuterRing();

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
					} else {
						API.setDeviceStatus(true)
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
                                            stopEquipment.setImageResource(R.drawable.btn_switch_equipment_state_open);
                                            deviceStatus = true;
                                            startInnerRing();
                                            startOuterRing();

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
					} //end of if (deviceStatus)
                } else {
                    stopInnerRing();
                    stopOuterRing();
                    if (null != listener){
                        listener.login();
                    }
//                    RedirectUtil.redirectToForResult(getActivity(), LoginActivity.class, RedirectUtil.BundleKey.LOGIN);
                }
                break;

            case R.id.lamp: {
                final WaitSetDialog.Builder wait = new WaitSetDialog.Builder(getActivity());
                wait.create().show();

                if (lamp.getTag().equals("false")){
                    API.setLampStatus(true)
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
                                        lampIv.setImageResource(R.drawable.icon_lamp_running);
                                        lamp.setImageResource(R.drawable.btn_switch_state_open);
                                        lamp.setTag("true");

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
                    //stopEquipment.setImageResource(R.drawable.btn_switch_equipment_state_open);
                } else {
                    API.setLampStatus(false)
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
                                        lampIv.setImageResource(R.drawable.icon_lamp_stop);
                                        lamp.setImageResource(R.drawable.btn_switch_state_close);
                                        lamp.setTag("false");

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
                    //stopEquipment.setImageResource(R.drawable.btn_switch_equipment_state_close);
                }
            }
            break;

            case R.id.door: {
                final WaitSetDialog.Builder wait = new WaitSetDialog.Builder(getActivity());
                wait.create().show();

                if (door.getTag().equals("false")) {
                    API.setDoorStatus(true)
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
                                        doorIv.setImageResource(R.drawable.icon_door_running);
                                        door.setImageResource(R.drawable.btn_switch_state_open);
                                        door.setTag("true");

                                        wait.succeed();
                                    } else {
                                        wait.error();
                                    }

                                    if (null != mDisposable && !mDisposable.isDisposed()) {
                                        mDisposable.dispose();
                                    }
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    wait.error();

                                    if (null != mDisposable && !mDisposable.isDisposed()) {
                                        mDisposable.dispose();
                                    }
                                }

                                @Override
                                public void onComplete() {
                                    if (null != mDisposable && !mDisposable.isDisposed()) {
                                        mDisposable.dispose();
                                    }
                                }
                            });
                } else {
                    API.setDoorStatus(false)
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
                                        doorIv.setImageResource(R.drawable.icon_door_stop);
                                        door.setImageResource(R.drawable.btn_switch_state_close);
                                        door.setTag("false");

                                        wait.succeed();
                                    } else {
                                        wait.error();
                                    }

                                    if (null != mDisposable && !mDisposable.isDisposed()) {
                                        mDisposable.dispose();
                                    }
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    wait.error();

                                    if (null != mDisposable && !mDisposable.isDisposed()) {
                                        mDisposable.dispose();
                                    }
                                }

                                @Override
                                public void onComplete() {
                                    if (null != mDisposable && !mDisposable.isDisposed()) {
                                        mDisposable.dispose();
                                    }
                                }
                            });
                }
            }
            break;

            case R.id.home_info:
                if (listener != null)
                    listener.onInfo();
                break;
        }
    }

    /**
     * 启动内环旋转动画
     */
    private void startInnerRing() {
        if(null != innerRing && null == innerRing.getAnimation()){
            Animation innerAnim = AnimationUtils.loadAnimation(oxygeneratorApplication.getContext(), R.anim.rotate_slow);
            innerAnim.setInterpolator(new LinearInterpolator());
            innerRing.startAnimation(innerAnim);
        }
    }

    /**
     * 停止内环旋转动画
     */
    private void stopInnerRing(){
        if (null != innerRing) {
            innerRing.clearAnimation();
        }
    }

    /**
     * 启动外环旋转动画
     */
    private void startOuterRing() {
        if(null != outerRing && null == outerRing.getAnimation()){
            Animation outerAnim = AnimationUtils.loadAnimation(oxygeneratorApplication.getContext(), R.anim.rotate);
            outerAnim.setInterpolator(new LinearInterpolator());
            outerRing.startAnimation(outerAnim);
        }
    }

    /**
     * 停止外环旋转动画
     */
    private void stopOuterRing(){
        if (null != outerRing){
            outerRing.clearAnimation();
        }
    }

    @Override
    public void onDestroy() {
        stopInnerRing();
        stopOuterRing();

        super.onDestroy();
    }

    /**
     * 设置监听
     * @param listener
     */
    public void setFragmentListener(FragmentListener listener){
        this.listener = listener;
    }
}
