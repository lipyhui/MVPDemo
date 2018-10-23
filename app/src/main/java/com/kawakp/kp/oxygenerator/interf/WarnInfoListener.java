package com.kawakp.kp.oxygenerator.interf;

import android.app.Activity;

/**
 * Created by penghui.li on 2017/6/19.
 */

public interface WarnInfoListener {
    Activity getSuperActivity();
    void onWarning(boolean b);
}
