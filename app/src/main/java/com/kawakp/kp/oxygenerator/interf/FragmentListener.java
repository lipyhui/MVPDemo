package com.kawakp.kp.oxygenerator.interf;

import android.app.Activity;

import com.kawakp.kp.oxygenerator.constant.PageConstant;

/**
 * Created by penghui.li on 2017/6/19.
 */

public interface FragmentListener {
    //public void logout();
    Activity getActivity();
    boolean isLogin();
    PageConstant getCurrentSet();
    void currentSet(PageConstant page);
    void login();
    void onInfo();
}
