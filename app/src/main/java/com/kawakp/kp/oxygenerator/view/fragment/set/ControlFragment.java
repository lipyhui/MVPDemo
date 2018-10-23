package com.kawakp.kp.oxygenerator.view.fragment.set;

import android.view.View;
import android.widget.ImageView;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.app.oxygeneratorApplication;
import com.kawakp.kp.oxygenerator.base.BaseFragment;
import com.kawakp.kp.oxygenerator.constant.UpdateConstant;
import com.kawakp.kp.oxygenerator.interf.WarnInfoListener;
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
 * Use the {@link ControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControlFragment extends BaseFragment {

    //加压
    //@BindView(R2.id.p_add)
    ImageView pAdd;
    //@BindView(R2.id.p_add_iv)
    ImageView pAddIv;

    //卸压
    //@BindView(R2.id.p_del)
    ImageView pDel;
    //@BindView(R2.id.p_del_iv)
    ImageView pDelIv;

    //弥散氧
    //@BindView(R2.id.oo_dispersion)
    ImageView ooDispersion;
    //@BindView(R2.id.oo_dispersion_iv)
    ImageView ooDispersionIv;

    //呼吸氧
    //@BindView(R2.id.oo_breath)
    ImageView ooBreath;
    //@BindView(R2.id.oo_breath_iv)
    ImageView ooBreathIv;

    //用于切换界面和推出时取消订阅
    private CompositeDisposable composite = new CompositeDisposable();

    //判断 Fragment 是否处于隐藏状态
    //private boolean polling = true;

    private WarnInfoListener warnListener;

    public ControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ControlFragment newInstance() {
        ControlFragment fragment = new ControlFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_set_control;
    }

    @Override
    public void initView(View view) {
        pAdd = (ImageView) view.findViewById(R.id.p_add);
        pAddIv = (ImageView) view.findViewById(R.id.p_add_iv);
        pDel = (ImageView) view.findViewById(R.id.p_del);
        pDelIv = (ImageView) view.findViewById(R.id.p_del_iv);
        ooDispersion = (ImageView) view.findViewById(R.id.oo_dispersion);
        ooDispersionIv = (ImageView) view.findViewById(R.id.oo_dispersion_iv);
        ooBreath = (ImageView) view.findViewById(R.id.oo_breath);
        ooBreathIv = (ImageView) view.findViewById(R.id.oo_breath_iv);

        pAdd.setOnClickListener(this);
        pDel.setOnClickListener(this);
        ooDispersion.setOnClickListener(this);
        ooBreath.setOnClickListener(this);
    }

    @Override
    public void initData() {
        updateInfo();
        updateStatus();
    }

    @Override
    protected void hideFragment() {
        //polling = false;
        composite.clear();
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
     * 更新开关状态
     */
    private void updateStatus(){
        composite.add(API.getSetControlStatus()
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
                            pAddIv.setImageResource(R.drawable.icon_p_add_open);
                            pAdd.setImageResource(R.drawable.btn_switch_state_open);
                            pAdd.setTag("true");
                        } else {
                            pAddIv.setImageResource(R.drawable.icon_p_add_close);
                            pAdd.setImageResource(R.drawable.btn_switch_state_close);
                            pAdd.setTag("false");
                        }

                        if (booleen[1]){
                            ooDispersionIv.setImageResource(R.drawable.icon_oo_dispersion_open);
                            ooDispersion.setImageResource(R.drawable.btn_switch_state_open);
                            ooDispersion.setTag("true");
                        } else {
                            ooDispersionIv.setImageResource(R.drawable.icon_oo_dispersion_close);
                            ooDispersion.setImageResource(R.drawable.btn_switch_state_close);
                            ooDispersion.setTag("false");
                        }

                        if (booleen[2]){
                            ooBreathIv.setImageResource(R.drawable.icon_oo_breath_open);
                            ooBreath.setImageResource(R.drawable.btn_switch_state_open);
                            ooBreath.setTag("true");
                        } else {
                            ooBreathIv.setImageResource(R.drawable.icon_oo_breath_close);
                            ooBreath.setImageResource(R.drawable.btn_switch_state_close);
                            ooBreath.setTag("false");
                        }

                        if (booleen[3]){
                            pDelIv.setImageResource(R.drawable.icon_p_del_open);
                            pDel.setImageResource(R.drawable.btn_switch_state_open);
                            pDel.setTag("true");
                        } else {
                            pDelIv.setImageResource(R.drawable.icon_p_del_close);
                            pDel.setImageResource(R.drawable.btn_switch_state_close);
                            pDel.setTag("false");
                        }
                    }
                }));
    }

    @Override
    public void onClick(View v) {
        final WaitSetDialog.Builder wait = new WaitSetDialog.Builder(getActivity());
        switch (v.getId()){
            case R.id.p_add:
                wait.create().show();
                if (pAdd.getTag().equals("false")){
                    API.setAddPStatus(true)
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
                                        pAddIv.setImageResource(R.drawable.icon_p_add_open);
                                        pAdd.setImageResource(R.drawable.btn_switch_state_open);
                                        pAdd.setTag("true");

                                        wait.succeed();
                                        updateStatus();
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
                    API.setAddPStatus(false)
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
                                        pAddIv.setImageResource(R.drawable.icon_p_add_close);
                                        pAdd.setImageResource(R.drawable.btn_switch_state_close);
                                        pAdd.setTag("false");

                                        wait.succeed();
                                        updateStatus();
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
                break;

            case R.id.p_del:
                wait.create().show();
                if (pDel.getTag().equals("false")){
                    API.setDelPStatus(true)
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
                                        pDelIv.setImageResource(R.drawable.icon_p_del_open);
                                        pDel.setImageResource(R.drawable.btn_switch_state_open);
                                        pDel.setTag("true");

                                        wait.succeed();
                                        updateStatus();
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
                    API.setDelPStatus(false)
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
                                        pDelIv.setImageResource(R.drawable.icon_p_del_close);
                                        pDel.setImageResource(R.drawable.btn_switch_state_close);
                                        pDel.setTag("false");

                                        wait.succeed();
                                        updateStatus();
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
                break;

            case R.id.oo_dispersion:
                wait.create().show();
                if (ooDispersion.getTag().equals("false")){
                    API.setDispersionStatus(true)
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
                                        ooDispersionIv.setImageResource(R.drawable.icon_oo_dispersion_open);
                                        ooDispersion.setImageResource(R.drawable.btn_switch_state_open);
                                        ooDispersion.setTag("true");

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
                    API.setDispersionStatus(false)
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
                                        ooDispersionIv.setImageResource(R.drawable.icon_oo_dispersion_close);
                                        ooDispersion.setImageResource(R.drawable.btn_switch_state_close);
                                        ooDispersion.setTag("false");

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
                break;

            case R.id.oo_breath:
                wait.create().show();
                if (ooBreath.getTag().equals("false")){
                    API.setBreathStatus(true)
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
                                        ooBreathIv.setImageResource(R.drawable.icon_oo_breath_open);
                                        ooBreath.setImageResource(R.drawable.btn_switch_state_open);
                                        ooBreath.setTag("true");

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
                    API.setBreathStatus(false)
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
                                        ooBreathIv.setImageResource(R.drawable.icon_oo_breath_close);
                                        ooBreath.setImageResource(R.drawable.btn_switch_state_close);
                                        ooBreath.setTag("false");

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
                break;

            default:
                break;
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
