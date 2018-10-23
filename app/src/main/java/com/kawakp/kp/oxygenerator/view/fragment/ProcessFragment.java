package com.kawakp.kp.oxygenerator.view.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.app.oxygeneratorApplication;
import com.kawakp.kp.oxygenerator.base.BaseFragment;
import com.kawakp.kp.oxygenerator.constant.UpdateConstant;
import com.kawakp.kp.oxygenerator.interf.FragmentListener;
import com.kawakp.kp.oxygenerator.util.StringDecimalUtil;
import com.kawakp.kp.oxygenerator.widget.animatorPath.ViewPath;
import com.kawakp.kp.oxygenerator.widget.animatorPath.ViewPathEvaluator;
import com.kawakp.kp.oxygenerator.widget.animatorPath.ViewPoint;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link ProcessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProcessFragment extends BaseFragment {

    //@BindView(R2.id.process_plc_time)
    TextView plcTime;

    //消息
    //@BindView(R2.id.process_info)
    ImageView info;

    //压力、浓度显示
    //@BindView(R2.id.process_p)
    TextView processP;
    //@BindView(R2.id.process_con)
    TextView processCon;

    //过滤空气
    //@BindView(R2.id.air_before)
    View beforeAir;
    //@BindView(R2.id.air_after)
    View afterAir;

    //压缩机
    //@BindView(R2.id.compress_l)
    View compressL;
    //@BindView(R2.id.compress_air_l)
    View compressAirL;
    //@BindView(R2.id.compress_r)
    View compressR;
    //@BindView(R2.id.compress_air_r)
    View compressAirR;

    //风扇
    //@BindView(R2.id.condenser)
    View condenser;

    //阀
    //@BindView(R2.id.fa)
    ImageView fa;

    //箭头
    //@BindView(R2.id.arrow_r_air)
    View arrow;
    View arrowTwo;
    View arrowTree;

    //呼吸氧
    private boolean breathOO = false;
    //弥散氧
    private boolean dispersionOO = false;
    //空气
    private boolean airOO = false;

    //判断动画是否已经结束
    private boolean animationEnd = true;

    //用于切换界面和推出时取消订阅
    private CompositeDisposable composite = new CompositeDisposable();

    //判断 Fragment 是否处于隐藏状态
    //private boolean polling = true;

    private FragmentListener listener;

    public ProcessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProcessFragment newInstance() {
        ProcessFragment fragment = new ProcessFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_process;
    }

    @Override
    public void initView(View view) {
        plcTime = (TextView) view.findViewById(R.id.process_plc_time);
        info = (ImageView) view.findViewById(R.id.process_info);
        processP = (TextView) view.findViewById(R.id.process_p);
        processCon = (TextView) view.findViewById(R.id.process_con);
        beforeAir = view.findViewById(R.id.air_before);
        afterAir = view.findViewById(R.id.air_after);
        compressL = view.findViewById(R.id.compress_l);
        compressAirL = view.findViewById(R.id.compress_air_l);
        compressR = view.findViewById(R.id.compress_r);
        compressAirR = view.findViewById(R.id.compress_air_r);
        condenser = view.findViewById(R.id.condenser);
        fa = (ImageView) view.findViewById(R.id.fa);
        arrow = view.findViewById(R.id.arrow_r_air);
        arrowTwo = view.findViewById(R.id.arrow_r_air_two);
        arrowTree = view.findViewById(R.id.arrow_r_air_three);

        info.setOnClickListener(this);
    }

    @Override
    public void initData() {
        updateTime();

//        updateCondenser();
        updateInfo();
        updateData();
        updateDeviceStatus();
        updateCompressStatus();
    }

    @Override
    protected void hideFragment() {
        //polling = false;

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
     * 更新风扇信息
     */
    private void updateCondenser(){
        composite.add(API.isCondenserStart()
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
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        if (aBoolean)
                            startCondenserRing();   //启动风扇动画
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
                    if (booleen[0] || booleen[1] || booleen[2]){
                        info.setImageResource(R.drawable.icon_info_red);
                    }else {
                        info.setImageResource(R.drawable.icon_info);
                    }
                }
            }));
    }

    /**
     * 更新数据(压力、浓度)
     */
    private void updateData(){
     composite.add(API.getGasholderPAndCon()
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
                    strings[0] = "压力: " + StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[0])/10f), 1) + "Kpa";
                    strings[1] = "浓度: " + StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[1])/100f), 2) + "%";
                    return strings;
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<String[]>() {
                @Override
                public void accept(@NonNull String[] s) throws Exception {
                    processP.setText(s[0]);
                    processCon.setText(s[1]);
                }
            }));
    }

    /**
     * 更新设备相关控制状态(空气、弥散氧、呼吸氧、卸压)
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
                        airOO = booleen[0];
                        dispersionOO = booleen[1];
                        breathOO = booleen[2];
                    }
                }));
    }

    /**
     * 更新流程拟态动画
     */
    private void updateCompressStatus(){
     composite.add(API.isCompressStart()
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
            .observeOn(Schedulers.newThread())
            .subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(@NonNull Boolean aBoolean) throws Exception {
                    if (aBoolean)
                        handler.sendEmptyMessage(0);
                }
            }));
}

    /**
     * 空气过滤动画
     */
    private void airAnimation(){
//        path.lineTo(200,200);
//        path.curveTo(100,400,200,600,300,300);
//        path.lineTo(500,300);

        //过滤前半部分
        ViewPath beforePath = new ViewPath(); //偏移坐标
        beforePath.moveTo(0,0);
        beforePath.lineTo(50,0);

        ObjectAnimator before = ObjectAnimator.ofObject(this,"before",new ViewPathEvaluator(),beforePath.getPoints().toArray());
        before.setInterpolator(new AccelerateDecelerateInterpolator());
        //anim.setRepeatCount(-1);

        before.setDuration(800);

        //过滤后半部分
        ViewPath afterPath = new ViewPath(); //偏移坐标
        afterPath.moveTo(0,0);
        afterPath.lineTo(50,0);

        ObjectAnimator after = ObjectAnimator.ofObject(this,"after",new ViewPathEvaluator(),afterPath.getPoints().toArray());
        after.setInterpolator(new AccelerateDecelerateInterpolator());
        //anim.setRepeatCount(-1);

        after.setDuration(800);

        //启动动画
        before.start();
        after.start();
    }

    /**
     * 过滤前空气动画的响应
     * @param newLoc
     */
    public void setBefore(ViewPoint newLoc){
        beforeAir.setTranslationX(newLoc.x);
        beforeAir.setTranslationY(newLoc.y);
    }

    /**
     * 过滤后空气动画的响应
     * @param newLoc
     */
    public void setAfter(ViewPoint newLoc){
        afterAir.setTranslationX(newLoc.x);
        afterAir.setTranslationY(newLoc.y);
    }

    /**
     * 压缩机动画
     */
    private void compressAnimation(){
        //左侧压缩机下压
        ObjectAnimator compressLAnimationDown = ObjectAnimator.ofFloat(compressL, "translationY", 0.0f, 10f);
        compressLAnimationDown.setDuration(600);
        compressLAnimationDown.start();

        //左侧压缩机归为
        ObjectAnimator compressLAnimationUp = ObjectAnimator.ofFloat(compressL, "translationY", 10f, 0f);
        compressLAnimationUp.setStartDelay(600);
        compressLAnimationUp.setDuration(600);
        compressLAnimationUp.start();

        //左侧压缩机空气下压
        ObjectAnimator compressAirLAnimationDown = ObjectAnimator.ofFloat(compressAirL, "translationY", 0.0f, 30f);
        compressAirLAnimationDown.setStartDelay(1200);
        compressAirLAnimationDown.setDuration(600);
        compressAirLAnimationDown.start();
        compressAirLAnimationDown.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                compressAirL.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                compressAirL.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        //左侧压缩机空气归为
        ObjectAnimator compressAirLAnimationUp = ObjectAnimator.ofFloat(compressAirL, "translationY", 30f, 0f);
        compressAirLAnimationUp.setStartDelay(1800);
        compressAirLAnimationUp.setDuration(200);
        compressAirLAnimationUp.start();

        //右侧压缩机下压
        ObjectAnimator compressRAnimationDown = ObjectAnimator.ofFloat(compressR, "translationY", 0.0f, 10f);
        compressRAnimationDown.setDuration(600);
        compressRAnimationDown.start();

        //右侧压缩机归为
        ObjectAnimator compressRAnimationUp = ObjectAnimator.ofFloat(compressR, "translationY", 10f, 0f);
        compressRAnimationUp.setStartDelay(600);
        compressRAnimationUp.setDuration(600);
        compressRAnimationUp.start();

        //右侧压缩机空气下压
        ObjectAnimator compressAirRAnimationDown = ObjectAnimator.ofFloat(compressAirR, "translationY", 0.0f, 30f);
        compressAirRAnimationDown.setStartDelay(1200);
        compressAirRAnimationDown.setDuration(600);
        compressAirRAnimationDown.start();
        compressAirRAnimationDown.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                compressAirR.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                compressAirR.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        //右侧压缩机空气归为
        ObjectAnimator compressAirRAnimationUp = ObjectAnimator.ofFloat(compressAirR, "translationY", 30f, 0f);
        compressAirRAnimationUp.setStartDelay(1800);
        compressAirRAnimationUp.setDuration(200);
        compressAirRAnimationUp.start();
    }

    /**
     * 启动风扇旋转动画
     */
    private void startCondenserRing() {
     /*   Animation condenserAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.condenser);
        condenserAnim.setInterpolator(new LinearInterpolator());
        if(condenserAnim != null){
            condenser.startAnimation(condenserAnim);
        }*/

     /*   RotateAnimation condenserAnim =new RotateAnimation(0f,90f,Animation.RELATIVE_TO_SELF,
                0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        condenserAnim.setDuration(300);//设置动画持续时间
        condenserAnim.setFillAfter(true);
        condenser.setAnimation(condenserAnim);
        condenserAnim.startNow();*/
    }

    /**
     * 停止风扇转动动画
     */
    private void stopCondenserRing(){
        condenser.clearAnimation();
    }


    /**
     * 箭头路径动画
     */
    public void arrowAnimation() {
        arrow.setVisibility(View.VISIBLE);
        arrowTwo.setVisibility(View.VISIBLE);
        arrowTree.setVisibility(View.VISIBLE);

        if (airOO) {
            //气体1右移
            ObjectAnimator faAnimationRight = ObjectAnimator.ofFloat(arrow, "translationX", 0f, 30f);
            faAnimationRight.setDuration(600);
            faAnimationRight.start();

            //气体1归为
            ObjectAnimator faAnimationLeft = ObjectAnimator.ofFloat(arrow, "translationX", 30f, 0f);
            faAnimationLeft.setStartDelay(600);
            faAnimationLeft.setDuration(200);
            faAnimationLeft.start();

            faAnimationLeft.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    arrow.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

        if (dispersionOO) {
            //气体2右移
            ObjectAnimator fa2AnimationRight = ObjectAnimator.ofFloat(arrowTwo, "translationX", 0f, 30f);
            fa2AnimationRight.setDuration(600);
            fa2AnimationRight.start();

            //气体1归为
            ObjectAnimator fa2AnimationLeft = ObjectAnimator.ofFloat(arrowTwo, "translationX", 30f, 0f);
            fa2AnimationLeft.setStartDelay(600);
            fa2AnimationLeft.setDuration(200);
            fa2AnimationLeft.start();

            fa2AnimationLeft.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    arrowTwo.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

        if (breathOO) {
            //气体3右移
            ObjectAnimator fa3AnimationRight = ObjectAnimator.ofFloat(arrowTree, "translationX", 0f, 30f);
            fa3AnimationRight.setDuration(600);
            fa3AnimationRight.start();

            //气体1归为
            ObjectAnimator fa3AnimationLeft = ObjectAnimator.ofFloat(arrowTree, "translationX", 30f, 0f);
            fa3AnimationLeft.setStartDelay(600);
            fa3AnimationLeft.setDuration(200);
            fa3AnimationLeft.start();

            fa3AnimationLeft.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    arrowTree.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:

                    //判断一组动画是否执行完成，若没完成不开始下一组动画
                    if (!animationEnd)
                        return;

                    animationEnd = false;   //设置一组动画开始标识

                    beforeAir.setVisibility(View.VISIBLE);
                    afterAir.setVisibility(View.VISIBLE);

                    airAnimation();
                    handler.sendEmptyMessageDelayed(1, 1000);   //动画后200ms进入下一动画
                    break;

                case 1:
                    beforeAir.setVisibility(View.INVISIBLE);
                    afterAir.setVisibility(View.INVISIBLE);

                    compressAnimation();
                    //handler.sendEmptyMessageDelayed(2, 2200);
                    handler.sendEmptyMessageDelayed(3, 2200);
                    break;

                case 2:
                   // startCondenserRing();
                    //handler.sendEmptyMessageDelayed(3, 1400);
                    break;

                case 3:
                    fa.setImageResource(R.drawable.process_fa_one);
                    handler.sendEmptyMessageDelayed(4, 400);
                    break;

                case 4:
                    fa.setImageResource(R.drawable.process_fa_two);
                    handler.sendEmptyMessageDelayed(5, 1000);
                    break;

                case 5:
                    arrowAnimation();
                    handler.sendEmptyMessageDelayed(6, 1000);
                    break;

                case 6:
                    animationEnd = true;   //设置一组动画结束标识，此处需要确保一组动画都已经执行完成。
                    composite.add(API.isCompressStart()
                                .observeOn(Schedulers.newThread())
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                                        if (aBoolean && !isHidden())
                                            handler.sendEmptyMessage(0);
                                    }
                                }));
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.process_info:
                if (listener != null)
                    listener.onInfo();
                break;
        }
    }

    @Override
    public void onDestroy() {
        stopCondenserRing(); //停止风扇旋转动画
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
