package com.kawakp.kp.oxygenerator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by penghui.li on 2017/6/22.
 */

public class StringDecimalUtil {

    /**
     * 带小数位的数字符串，限制小数位,不处理四舍五入
     *
     * @param str   纯数字的带小数的字符串
     * @param decimal   保留的小数位数(最好6以下，超过6需自行保证
     *                  str 的小数位，不然小数位可能小于最小小数位)
     * @return
     */
    public static String limitedDecimal(String str, int decimal){
        if (!isNumeric(str))
            return str;

        switch (str.lastIndexOf(".")) {
            case -1:
                if (str.length() == 0){
                    str = "0.000000";
                }else {
                    str += ".000000";
                }
                break;

            case 0:
                str = "0" + str + "000000";
                break;

            default:
                str += "000000";
                break;
        }
        return str.substring(0, str.indexOf(".") + decimal + 1);
    }

    /**
     * 利用正则表达式判断字符串是否是数字(可以带小数点)
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9|.]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
}
