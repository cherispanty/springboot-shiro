package org.chonglin.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * md5加密
 */

public class MD5
{
    private MD5(){}

    public static String md5(String text, String key)
    {
        return DigestUtils.md5Hex(DigestUtils.md5Hex(DigestUtils.md5Hex(text))+key);
    }

    public static boolean check(String text, String key, String md5)
    {
        String md5Text = DigestUtils.md5Hex(DigestUtils.md5Hex(DigestUtils.md5Hex(text))+key);
        if(md5 != null && md5.equals(md5Text))
            return true;
        return false;
    }

    public static void main(String[] args) {
//        System.out.println(md5(System.currentTimeMillis()+"",System.currentTimeMillis()+""));
        String text = "danny" + "danny123";
        String encypte = MD5.md5(text, "bc29710d3429524789abc15e06732834");
        System.out.println(encypte);
    }
}
