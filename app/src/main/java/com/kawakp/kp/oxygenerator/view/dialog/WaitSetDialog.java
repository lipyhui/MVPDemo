package com.kawakp.kp.oxygenerator.view.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.kawakp.kp.oxygenerator.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 等待PLC是否成功
 */
public class WaitSetDialog extends AlertDialog {

    protected WaitSetDialog(Context context, int theme) {
        super(context, theme);
    }
    protected WaitSetDialog(Context context) {
        super(context);
    }

    public static class Builder {

        private Activity context;
        private LayoutInflater inflater = null;
        private WaitSetDialog Mydialog;
        private ImageView waitBall;
        private ImageView wait;

        public Builder(Activity context) {
            if (null == context || context.isFinishing())
                return;

            this.context = context;
            this.inflater= LayoutInflater.from(context);
        }

        @SuppressLint("Override")
        public WaitSetDialog create() {
            Mydialog = new WaitSetDialog(context, R.style.NoBackGroundDialog);
            View view = inflater.inflate(R.layout.dialog_wait_set, null);
            waitBall = (ImageView) view.findViewById(R.id.wait_ball);
            wait = (ImageView) view.findViewById(R.id.wait_img);

            Animation innerAnim = AnimationUtils.loadAnimation(context, R.anim.wait);
            innerAnim.setInterpolator(new LinearInterpolator());
            if(innerAnim != null){
                wait.startAnimation(innerAnim);
            }

            Mydialog.setView(view, 0, 0, 0, 0);
			//Mydialog.getWindow().setDimAmount(0);//设置昏暗度为0(全透明)
            Mydialog.setCancelable(false);
            Mydialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
            Mydialog.show();
            return Mydialog;
        }

        /**
         * 显示成功并取消dialog
         */
        public void succeed() {
            wait.clearAnimation();
            wait.setImageResource(R.drawable.dialog_wait_succeed);
            Observable.timer(200, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        private Disposable mDisposable;
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            mDisposable = d;
                        }

                        @Override
                        public void onNext(@NonNull Long aLong) {
                            cancel();
                            if (null != mDisposable && !mDisposable.isDisposed()){
                                mDisposable.dispose();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            cancel();
                            if (null != mDisposable && !mDisposable.isDisposed()){
                                mDisposable.dispose();
                            }
                        }

                        @Override
                        public void onComplete() {
                            cancel();
                            if (null != mDisposable && !mDisposable.isDisposed()){
                                mDisposable.dispose();
                            }
                        }
                    });
        }

        /**
         * 显示失败并取消dialog
         */
        public void error() {
            wait.clearAnimation();
            wait.setImageResource(R.drawable.dialog_wait_error);
            waitBall.setImageResource(R.drawable.dialog_wait_ball_error);
            Observable.timer(400, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        private Disposable mDisposable;
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            mDisposable = d;
                        }

                        @Override
                        public void onNext(@NonNull Long aLong) {
                            cancel();
                            if (null != mDisposable && !mDisposable.isDisposed()){
                                mDisposable.dispose();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            cancel();
                            if (null != mDisposable && !mDisposable.isDisposed()){
                                mDisposable.dispose();
                            }
                        }

                        @Override
                        public void onComplete() {
                            cancel();
                            if (null != mDisposable && !mDisposable.isDisposed()){
                                mDisposable.dispose();
                            }
                        }
                    });
        }

        /**
         * 取消dialog
         */
        public void cancel(){
            Mydialog.dismiss();
        }
    }
}
