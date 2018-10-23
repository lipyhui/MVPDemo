package com.kawakp.kp.oxygenerator;

import android.view.View;

import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.base.BaseActivity;
import com.kawakp.kp.oxygenerator.util.CheckUtil;
import com.kawakp.kp.oxygenerator.util.RedirectUtil;
import com.kawakp.kp.oxygenerator.view.MainActivity;
import com.kawakp.kp.oxygenerator.view.password.PasswordActivity1;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;


public class StartActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_launch;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {
        redirectTo();
    }

    private void redirectTo() {
        API.getQuestion()
                .map(new Function<String[], Boolean>() {
                    @Override
                    public Boolean apply(@NonNull String[] strings) throws Exception {
                        return CheckUtil.isEmpty(strings[0]) || CheckUtil.isEmpty(strings[1])
                                || CheckUtil.isEmpty(strings[2]) || strings[2].length() < 7
                                || CheckUtil.isEmpty(strings[3]) || strings[3].length() < 7;
                    }
                })
                .delay(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Boolean b) {
                        if (b){
                            RedirectUtil.redirectTo(StartActivity.this, PasswordActivity1.class);
                        }else {
                            RedirectUtil.redirectTo(StartActivity.this, MainActivity.class);
                        }
                        StartActivity.this.finish();

                        if (null != disposable && !disposable.isDisposed())
                            disposable.dispose();
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
        //RedirectUtil.redirectTo(this, MainActivity.class);
        //RedirectUtil.redirectTo(this, PasswordActivity1.class);
    }
}
