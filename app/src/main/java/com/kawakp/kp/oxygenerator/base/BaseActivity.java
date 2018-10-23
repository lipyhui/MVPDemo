package com.kawakp.kp.oxygenerator.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.kawakp.kp.oxygenerator.app.oxygeneratorApplication;
import com.kawakp.kp.oxygenerator.interf.BaseViewInterface;
import com.kawakp.kp.oxygenerator.util.TDevice;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener, BaseViewInterface {

    protected LayoutInflater mInflater;

    //private Unbinder mUnbinder;

    @Override
    public Context getBaseContext() {
        return super.getBaseContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //添加activity到管理类
        oxygeneratorApplication.addActivity(this);

        //设置开机解锁屏幕
      /*  getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);*/
        //设置屏幕一直亮屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        onBeforeSetContentLayout();

        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        mInflater = getLayoutInflater();

        //mUnbinder = ButterKnife.bind(this);
        //mUnbinder = ButterKnife.bind(this, findViewById(android.R.id.content));

        //设置屏幕一直亮屏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        init(savedInstanceState);
        initView();
    }

    @Override
    protected void onStart() {
        initData();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.isFinishing()) {
            TDevice.hideSoftKeyboard(getCurrentFocus());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * This method is called before {@link AppCompatActivity#setContentView(int)}.
     */
    protected void onBeforeSetContentLayout() {
    }

    /**
     * Set layout id to activity.
     *
     * @return layoutId
     */
    protected int getLayoutId() {
        return 0;
    }

    /**
     * Get {@link View} from resId.
     *
     * @param resId
     * @return
     */
    protected View inflateView(int resId) {
        return mInflater.inflate(resId, null);
    }

    /**
     * {@link BaseActivity#init(Bundle)} before {@link BaseViewInterface#initView()}
     * and {@link BaseViewInterface#initData()}
     *
     * @param savedInstanceState
     */
    protected void init(Bundle savedInstanceState) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // mUnbinder.unbind();
    }
}
