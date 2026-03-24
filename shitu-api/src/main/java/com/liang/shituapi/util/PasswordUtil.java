package com.liang.shituapi.util;


import cn.hutool.crypto.digest.DigestUtil;

/**
 * 密码加密工具类
 */
public class PasswordUtil {

    /**
     * MD5加密
     */
    public static String encrypt(String password) {
        return DigestUtil.md5Hex(password);
    }

    /**
     * 验证密码
     */
    public static boolean verify(String inputPassword, String encryptedPassword) {
        return encrypt(inputPassword).equals(encryptedPassword);
    }

    /**
     * 生成随机密码
     */
    public static String generateRandomPassword() {
        return DigestUtil.md5Hex(String.valueOf(System.currentTimeMillis()));
    }
}
