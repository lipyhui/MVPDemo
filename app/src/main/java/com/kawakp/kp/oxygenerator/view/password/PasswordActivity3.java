package com.kawakp.kp.oxygenerator.view.password;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class PasswordActivity3 extends BaseActivity {

    //@BindView(R2.id.password3_prev)
    View prev;

    //@BindView(R2.id.password_go)
    View go;

    //第一位密码
    //@BindView(R2.id.password3_one_num)
    TextView oneNum;

    //第二位密码
    //@BindView(R2.id.password3_two_num)
    TextView twoNum;

    //密保问题
    private String question;
    private String answer;
    private String passwordOne;
    private String passwordTwo;

    //取消数据保存监听
    private Disposable disposable;

    /**
     * 页面同步和登录同步取消订阅变量
     */
    private Disposable pageDisposable;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_password3;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.password3_prev:
                onBackPressed();
                break;

            case R.id.password_go:
                //防止重复点击，多次跳转
                if (null != disposable && !disposable.isDisposed()) {
                    return;
                }

                Toast.makeText(this, "数据保存中，请耐心等待!", Toast.LENGTH_LONG).show();
                API.saveQuestion(question, answer, passwordOne, passwordTwo)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Boolean aBoolean) {
                        //Log.e("API_Boolean", "aBoolean = " + aBoolean);
                        if (aBoolean){
                            RedirectUtil.redirectTo(PasswordActivity3.this, MainActivity.class);
                            Toast.makeText(PasswordActivity3.this, "正在加载主页!", Toast.LENGTH_SHORT).show();
                            oxygeneratorApplication.clearActivity();
                        } else {
                            Toast.makeText(PasswordActivity3.this, "数据保存失败!", Toast.LENGTH_SHORT).show();
                        }

                        if (null != disposable && !disposable.isDisposed()) {
                            disposable.dispose();
                            disposable = null;
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (null != disposable && !disposable.isDisposed()) {
                            disposable.dispose();
                            disposable = null;
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (null != disposable && !disposable.isDisposed()) {
                            disposable.dispose();
                            disposable = null;
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void initView() {
        prev = findViewById(R.id.password3_prev);
        go = findViewById(R.id.password_go);
        oneNum = (TextView) findViewById(R.id.password3_one_num);
        twoNum = (TextView) findViewById(R.id.password3_two_num);

        prev.setOnClickListener(this);
        go.setOnClickListener(this);
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
                            RedirectUtil.redirectTo(PasswordActivity3.this, MainActivity.class);
                            oxygeneratorApplication.clearActivity();
                        }
                    }
                });

        Bundle bundle = getIntent().getBundleExtra(RedirectUtil.BundleKey.PASSWORD.getKey());
        if (null != bundle) {
            question = bundle.getString(PasswordActivity2.QUESTION).trim();
            answer = bundle.getString(PasswordActivity2.ANSWER).trim();
            passwordOne = bundle.getString(PasswordActivity2.ONE).trim();
            passwordTwo = bundle.getString(PasswordActivity2.TWO).trim();

            oneNum.setText(passwordOne);
            twoNum.setText(passwordTwo);
        }
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

    @Override
    public void onBackPressed() {
       /* Intent intent = new Intent();
        intent.putExtra(MainActivity.IS_LOGIN, isLogin);
        intent.putExtra(MainActivity.USER_HEADER, header);
        intent.putExtra(MainActivity.USER_NAME, name);
        setResult(RESULT_OK, intent);*/

        super.onBackPressed();
    }
}
