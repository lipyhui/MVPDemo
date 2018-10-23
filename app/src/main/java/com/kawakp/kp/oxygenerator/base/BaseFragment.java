package com.kawakp.kp.oxygenerator.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kawakp.kp.oxygenerator.interf.BaseFragmentInterface;

/**
 * 注意：
 * 如果 A 继承{@link BaseFragment}的类，A的子布局 B 也继承{@link BaseFragment}，
 * 需要在 A 手动调用{@link BaseFragment#onHiddenChanged(boolean)}来改变 B 的隐藏和显示状态
 *
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment implements android.view.View.OnClickListener, BaseFragmentInterface {

    protected LayoutInflater mInflater;

    //private Unbinder mUnbinder;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState != null) {
            String FRAGMENTS_TAG = "android:support:fragments";
            // remove掉保存的Fragment
            outState.remove(FRAGMENTS_TAG);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.mInflater = inflater;
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (getLayoutId() != 0){
            view = inflater.inflate(getLayoutId(), container, false);
        }

        //mUnbinder = ButterKnife.bind(this, view);

        initView(view);
        initData();

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            hideFragment();
        }else {
            initData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

 /*   @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //mUnbinder.unbind();
    }

    protected int getLayoutId() {
        return 0;
    }

    protected View inflateView(int resId) {
        return this.mInflater.inflate(resId, null);
    }

    protected void hideFragment(){}

    public boolean onBackPressed() {
        return false;
    }
}
