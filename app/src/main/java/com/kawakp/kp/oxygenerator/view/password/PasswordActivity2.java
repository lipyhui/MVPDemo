package com.kawakp.kp.oxygenerator.view.password;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class PasswordActivity2 extends BaseActivity {
    public static String QUESTION = "passwordQuestion";
    public static String ANSWER = "passwordAnswer";
    public static String ONE = "passwordOne";
    public static String ONE_CONFIRM = "passwordOneConfirm";
    public static String TWO = "passwordTwo";
    public static String TWO_CONFIRM = "passwordTwoConfirm";

    //@BindView(R2.id.password2_prev)
    View prev;

    //@BindView(R2.id.password2_next)
    View next;

    // 密保问题
    //@BindView(R2.id.password_question)
    EditText question;
    //@BindView(R2.id.password_answer)
    EditText answer;

    // 首位密码
    //@BindView(R2.id.password_one)
    EditText passwordOne;
    //@BindView(R2.id.password_one_confirm)
    EditText passwordOneConfirm;
    //@BindView(R2.id.eye_one)
    ImageView eyeOne;
    //@BindView(R2.id.password_one_confirm_iv)
    View oneConfirmIv;

    // 第二位密码
    //@BindView(R2.id.password_two)
    EditText passwordTwo;
    //@BindView(R2.id.password_two_confirm)
    EditText passwordTwoConfirm;
    //@BindView(R2.id.eye_two)
    ImageView eyeTwo;
    //@BindView(R2.id.password_two_confirm_iv)
    View twoConfirmIv;

    private boolean oneError = true;
    private boolean twoError = true;

    /**
     * 页面同步和登录同步取消订阅变量
     */
    private Disposable pageDisposable;

    //首位密码输入监听
    private TextWatcher oneWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
          /*  if (s.length() < 7 && passwordOneConfirm.getText().length() <= 0)
                return;*/

            if (passwordOne.getText().toString().equals(passwordOneConfirm.getText().toString()) && s.length() == 7){
                oneError = false;
                passwordOneConfirm.setBackgroundResource(R.drawable.password_input);
                oneConfirmIv.setVisibility(View.GONE);
            }else {
                oneError = true;
                passwordOneConfirm.setBackgroundResource(R.drawable.password_input_error);
                oneConfirmIv.setVisibility(View.VISIBLE);
            }
        }
    };

    //第二位密码输入监听
    private TextWatcher twoWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
           /* if (s.length() < 7 && passwordTwoConfirm.getText().length() <= 0)
                return;*/

            if (passwordTwo.getText().toString().equals(passwordTwoConfirm.getText().toString()) && s.length() == 7){
                passwordTwoConfirm.setBackgroundResource(R.drawable.password_input);
                twoConfirmIv.setVisibility(View.GONE);
                twoError = false;
            }else {
                passwordTwoConfirm.setBackgroundResource(R.drawable.password_input_error);
                twoConfirmIv.setVisibility(View.VISIBLE);
                twoError = true;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_password2;
    }

/*    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        question.setText(savedInstanceState.getString(QUESTION));
        answer.setText(savedInstanceState.getString(ANSWER));
        passwordOne.setText(savedInstanceState.getString(ONE));
        passwordOneConfirm.setText(savedInstanceState.getString(ONE_CONFIRM));
        passwordTwo.setText(savedInstanceState.getString(TWO));
        passwordTwoConfirm.setText(savedInstanceState.getString(TWO_CONFIRM));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUESTION, question.getText().toString());
        outState.putString(ANSWER, answer.getText().toString());
        outState.putString(ONE, passwordOne.getText().toString());
        outState.putString(ONE_CONFIRM, passwordOneConfirm.getText().toString());
        outState.putString(TWO, passwordTwo.getText().toString());
        outState.putString(TWO_CONFIRM, passwordTwoConfirm.getText().toString());
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.password2_prev:
                onBackPressed();
                break;

            case R.id.password2_next:
                if (question.getText().toString().trim().length() <= 0 || answer.getText().toString().trim().length() <= 0){
                    Toast.makeText(this, "密保问题不能为空!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (oneError){
                    Toast.makeText(this, "首位密码验证出错!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (twoError){
                    Toast.makeText(this, "第二位密码验证出错!", Toast.LENGTH_SHORT).show();
                    return;
                }

                RedirectUtil.redirectTo(this, PasswordActivity3.class, RedirectUtil.BundleKey.PASSWORD, updateBundle());
                break;
        }
    }

    @Override
    public void initView() {
        prev = findViewById(R.id.password2_prev);
        next = findViewById(R.id.password2_next);
        question = (EditText) findViewById(R.id.password_question);
        answer = (EditText) findViewById(R.id.password_answer);
        passwordOne = (EditText) findViewById(R.id.password_one);
        passwordOneConfirm = (EditText) findViewById(R.id.password_one_confirm);
        eyeOne = (ImageView) findViewById(R.id.eye_one);
        oneConfirmIv = findViewById(R.id.password_one_confirm_iv);
        passwordTwo= (EditText) findViewById(R.id.password_two);
        passwordTwoConfirm = (EditText) findViewById(R.id.password_two_confirm);
        eyeTwo = (ImageView) findViewById(R.id.eye_two);
        twoConfirmIv = findViewById(R.id.password_two_confirm_iv);

 /*        eyeOne.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //ToolToast.buildToast(getApplicationContext(),"down",1000).show();
                    passwordOne.setInputType(InputType.TYPE_CLASS_TEXT);

                }else{
                    //ToolToast.buildToast(getApplicationContext(),"up",1000).show();
                    passwordOne.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                passwordOne.setSelection(passwordOne.getText().length());
            }
        });

       eyeTwo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //ToolToast.buildToast(getApplicationContext(),"down",1000).show();
                    passwordTwo.setInputType(InputType.TYPE_CLASS_TEXT);

                }else{
                    //ToolToast.buildToast(getApplicationContext(),"up",1000).show();
                    passwordTwo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                passwordTwo.setSelection(passwordTwo.getText().length());
            }
        });
*/

        eyeOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eyeOne.getTag().equals("false")){
                    passwordOne.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    eyeOne.setImageResource(R.drawable.icon_eye_visible);
                    eyeOne.setTag("true");
                } else {
                    passwordOne.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeOne.setImageResource(R.drawable.icon_eye_invisible);
                    eyeOne.setTag("false");
                }
                passwordOne.setSelection(passwordOne.length());
            }
        });

        eyeTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eyeTwo.getTag().equals("false")){
                    passwordTwo.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    eyeTwo.setImageResource(R.drawable.icon_eye_visible);
                    eyeTwo.setTag("true");
                } else {
                    passwordTwo.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeTwo.setImageResource(R.drawable.icon_eye_invisible);
                    eyeTwo.setTag("false");
                }
                passwordTwo.setSelection(passwordTwo.length());
            }
        });

        passwordOne.addTextChangedListener(oneWatcher);
        passwordOneConfirm.addTextChangedListener(oneWatcher);
        passwordTwo.addTextChangedListener(twoWatcher);
        passwordTwoConfirm.addTextChangedListener(twoWatcher);

        prev.setOnClickListener(this);
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
                            RedirectUtil.redirectTo(PasswordActivity2.this, MainActivity.class);
                            oxygeneratorApplication.clearActivity();
                        }
                    }
                });

        Bundle bundle = getIntent().getBundleExtra(RedirectUtil.BundleKey.PASSWORD.getKey());
        if (null != bundle) {
            question.setText(bundle.getString(QUESTION));
            answer.setText(bundle.getString(ANSWER));
            passwordOne.setText(bundle.getString(ONE));
            passwordOneConfirm.setText(bundle.getString(ONE_CONFIRM));
            passwordTwo.setText(bundle.getString(TWO));
            passwordTwoConfirm.setText(bundle.getString(TWO_CONFIRM));
        }
    }

    /**
     * 把当前界面收输入数据绑定到 Bundle 中
     *
     * @return
     */
    private Bundle updateBundle(){
        Bundle bundle = new Bundle();
        bundle.putString(QUESTION, question.getText().toString().trim());
        bundle.putString(ANSWER, answer.getText().toString().trim());
        bundle.putString(ONE, passwordOne.getText().toString());
        bundle.putString(ONE_CONFIRM, passwordOneConfirm.getText().toString());
        bundle.putString(TWO, passwordTwo.getText().toString());
        bundle.putString(TWO_CONFIRM, passwordTwoConfirm.getText().toString());

        return bundle;
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
        Intent intent = new Intent();
        intent.putExtra(RedirectUtil.BundleKey.PASSWORD.getKey(), updateBundle());
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }
}
