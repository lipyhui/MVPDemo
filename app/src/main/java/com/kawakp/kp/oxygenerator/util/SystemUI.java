package com.kawakp.kp.oxygenerator.util;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;

/**
 * Created by penghui.li on 2017/6/13.
 */

public class SystemUI {
    /**
     * 获取 StatusBar 高度
     *
     * @param activity
     * @return
     */
    public static int getStatusBarHeight(Activity activity) {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        Log.v("SystemUI", "Status height:" + height);
        return height;
    }

    /**
     * 获取 NavigationBar 高度
     *
     * @param activity
     * @return
     */
    public static int getNavigationBarHeight(Activity activity) {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        Log.v("SystemUI", "Navi height:" + height);
        return height;
    }
}
