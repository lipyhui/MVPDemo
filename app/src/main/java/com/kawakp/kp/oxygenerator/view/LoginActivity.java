package com.kawakp.kp.oxygenerator.view;

import android.content.Intent;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.base.BaseActivity;
import com.kawakp.kp.oxygenerator.constant.UpdateConstant;
import com.kawakp.kp.oxygenerator.util.CheckUtil;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class LoginActivity extends BaseActivity {

    //@BindView(R2.id.login_plc_time)
    TextView plcTime;

    //@BindView(R2.id.back)
    View back;
    //@BindView(R2.id.user_name)
    EditText userName;
    //@BindView(R2.id.password)
    EditText password;
    //@BindView(R2.id.eye)
    ImageView eye;
    //@BindView(R2.id.login)
    View login;

    private boolean isLogin = false;
    private String header = "header";
    private String name = "kawakp_login";

    //用户名
    private String NAME = "kawadmin";
    //时分时间
    private String time = "4321";
    //星期
    private int week = 1;
    //首位动态密码
    private String passwordOne = "";
    //第二位动态密码
    private String passwordTwo = "";

    //时间订阅操作量
    private Disposable disposable;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                isLogin = false;
                onBackPressed();
                break;

            case R.id.login:
                if (CheckUtil.isEmpty(passwordOne) || CheckUtil.isEmpty(passwordTwo)){
                    Toast.makeText(this, "用户名或密码错误！", Toast.LENGTH_LONG).show();
                    getPassword();
                    return;
                }

                if (CheckUtil.isEmpty(time)){
                    Toast.makeText(this, "用户名或密码错误！", Toast.LENGTH_LONG).show();
                    updateTime();
                    return;
                }

                if (CheckUtil.isEmpty(userName.getText().toString().trim())){
                    Toast.makeText(this, "用户名不能为空！", Toast.LENGTH_LONG).show();
                    return;
                }
                name = userName.getText().toString().trim();
                if (!name.equals(NAME)){
                    Toast.makeText(this, "用户名或密码错误！", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!(passwordOne.substring(week -1, week) + passwordTwo.substring(week - 1, week) + time).trim().equals(password.getText().toString().trim())){
                    Toast.makeText(this, "用户名或密码错误！", Toast.LENGTH_LONG).show();
                    return;
                }

                isLogin = true;
                header = "test";
                onBackPressed();
                break;
        }
    }

    @Override
    public void initView() {
        plcTime = (TextView) findViewById(R.id.login_plc_time);
        back = findViewById(R.id.back);
        userName = (EditText) findViewById(R.id.user_name);
        password = (EditText) findViewById(R.id.password);
        eye = (ImageView) findViewById(R.id.eye);
        login = findViewById(R.id.login);

        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eye.getTag().equals("false")){
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    eye.setImageResource(R.drawable.icon_eye_visible);
                    eye.setTag("true");
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eye.setImageResource(R.drawable.icon_eye_invisible);
                    eye.setTag("false");
                }
                password.setSelection(password.length());
            }
        });

        back.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void initData() {
        getPassword();

        updateTime();
    }

    /**
     * 获取动态密码
     */
    private void getPassword(){
        API.getQuestion()
                .subscribe(new Observer<String[]>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull String[] strings) {
                        passwordOne = strings[2].trim();
                        passwordTwo = strings[3].trim();

                        if (disposable != null && !disposable.isDisposed())
                            disposable.dispose();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (disposable != null && !disposable.isDisposed())
                            disposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (disposable != null && !disposable.isDisposed())
                            disposable.dispose();
                    }
                });
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
                        if (!LoginActivity.this.isFinishing())
                            plcTime.setText(s);

                        time = s.substring(11, 16);
                        time = time.replace(":", "").trim();

                        switch (s.substring(19)){
                            case "一":
                                week = 1;
                                break;

                            case "二":
                                week = 2;
                                break;

                            case "三":
                                week = 3;
                                break;

                            case "四":
                                week = 4;
                                break;

                            case "五":
                                week = 5;
                                break;

                            case "六":
                                week = 6;
                                break;

                            case "日":
                                week = 7;
                                break;

                            default:
                                week = 1;
                                break;
                        }

                        //Log.e("loginWeek", "week = " + week);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.IS_LOGIN, isLogin);
        intent.putExtra(MainActivity.USER_HEADER, header);
        intent.putExtra(MainActivity.USER_NAME, name);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }
}
