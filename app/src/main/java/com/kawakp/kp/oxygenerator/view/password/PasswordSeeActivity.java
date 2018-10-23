package com.kawakp.kp.oxygenerator.view.password;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.base.BaseActivity;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class PasswordSeeActivity extends BaseActivity {

    //@BindView(R2.id.password_back)
    View back;

    //设置密码修改是否可见
    //@BindView(R2.id.password_see)
    View passwordSee;

    //密保问题
    //@BindView(R2.id.password_see_question)
    TextView passwordQuestion;
    //@BindView(R2.id.password_see_answer)
    EditText passwordAnswer;
    //@BindView(R2.id.password_see_answer_confirm_iv)
    ImageView answerConfirm;

    //@BindView(R2.id.password_see_submit)
    View submit;

    //动态密码
    //@BindView(R2.id.password_see_one)
    TextView passwordOne;
    //@BindView(R2.id.password_see_two)
    TextView passwordTwo;

    //保存读取的回答数据
    private String answer;

    //保存是否回答正确
    private boolean answerTrue = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_password_see;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.password_back:
                onBackPressed();
                break;

            case R.id.password_see_submit:
                if (answerTrue){
                    answerConfirm.setImageResource(R.drawable.icon_right);
                    passwordAnswer.setBackgroundResource(R.drawable.password_input);
                    passwordSee.setVisibility(View.VISIBLE);
                }else {
                    Toast.makeText(this, "密保问题回答错误!", Toast.LENGTH_SHORT).show();
                    answerConfirm.setImageResource(R.drawable.icon_error);
                    passwordAnswer.setBackgroundResource(R.drawable.password_input_error);
                    passwordSee.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }

    @Override
    public void initView() {
        back = findViewById(R.id.password_back);
        passwordSee = findViewById(R.id.password_see);
        passwordQuestion = (TextView) findViewById(R.id.password_see_question);
        passwordAnswer = (EditText) findViewById(R.id.password_see_answer);
        answerConfirm = (ImageView) findViewById(R.id.password_see_answer_confirm_iv);
        submit = findViewById(R.id.password_see_submit);
        passwordOne = (TextView) findViewById(R.id.password_see_one);
        passwordTwo = (TextView) findViewById(R.id.password_see_two);

        back.setOnClickListener(this);

        passwordAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(answer)){
                    answerConfirm.setImageResource(R.drawable.icon_right);
                    passwordAnswer.setBackgroundResource(R.drawable.password_input);
                    answerTrue = true;
                }else {
                    answerConfirm.setImageResource(R.drawable.icon_error);
                    passwordAnswer.setBackgroundResource(R.drawable.password_input_error);
                    answerTrue = false;
                }

                if (s.length() > 0){
                    answerConfirm.setVisibility(View.VISIBLE);
                }else {
                    answerConfirm.setVisibility(View.INVISIBLE);
                }
            }
        });

        submit.setOnClickListener(this);
    }

    @Override
    public void initData() {
        API.getQuestion()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String[]>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull String[] strings) {
                        if (strings[0].lastIndexOf("?") > 0 || strings[0].lastIndexOf("？") > 0) {
                            passwordQuestion.setText(strings[0]);
                        }else {
                            passwordQuestion.setText(strings[0] + "?");
                        }

                        answer = strings[1];
                        passwordOne.setText(strings[2]);
                        passwordTwo.setText(strings[3]);

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
