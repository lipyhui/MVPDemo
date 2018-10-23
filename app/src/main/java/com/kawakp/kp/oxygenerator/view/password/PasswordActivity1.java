package com.kawakp.kp.oxygenerator.view.password;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.app.oxygeneratorApplication;
import com.kawakp.kp.oxygenerator.base.BaseActivity;
import com.kawakp.kp.oxygenerator.constant.PageConstant;
import com.kawakp.kp.oxygenerator.constant.UpdateConstant;
import com.kawakp.kp.oxygenerator.util.RedirectUtil;
import com.kawakp.kp.oxygenerator.view.MainActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class PasswordActivity1 extends BaseActivity {

    //@BindView(R2.id.password1_next)
    View next;

    private Bundle bundle = null;

    /**
     * 页面同步和登录同步取消订阅变量
     */
    private Disposable pageDisposable;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_password1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.password1_next:
                RedirectUtil.redirectToForResult(this, PasswordActivity2.class, RedirectUtil.BundleKey.PASSWORD, bundle);
                break;
        }
    }

    @Override
    public void initView() {
        next = findViewById(R.id.password1_next);

        next.setOnClickListener(this);
    }

    @Override
    public void initData() {
        API.setPage(PageConstant.PASSWORD);
        pageDisposable = API.getPageAndLoginStatus()
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
                .subscribe(new Consumer<String[]>() {
                    @Override
                    public void accept(@NonNull String[] strings) throws Exception {
                        if (Integer.parseInt(strings[0]) != 7){
                            RedirectUtil.redirectTo(PasswordActivity1.this, MainActivity.class);
                            oxygeneratorApplication.clearActivity();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RedirectUtil.BundleKey.PASSWORD.getRequestCode() == requestCode && resultCode == RESULT_OK)
            bundle = data.getBundleExtra(RedirectUtil.BundleKey.PASSWORD.getKey());
    }

    @Override
    protected void onPause() {
        if (pageDisposable != null && !pageDisposable.isDisposed()){
            pageDisposable.dispose();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
