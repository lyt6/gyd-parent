package com.glodon.gyd.common.db.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

public class AesUtil {

    //对称加密密钥key
    public static final String keyStr = "EC/Z+S7c3EFJa2dtvLyekg==";
    //将Base64编码的秘钥的格式进行解码转换
    static byte[] keyByte = Base64.decode(keyStr);

    //随机生成密钥
    //static byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();

    //构建
    static SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, keyByte);

    public static String encrypt(String content){
        return aes.encryptHex(content);
    }

    public static String decrypt(String content){
        return aes.decryptStr(content);
    }

    public static void main(String[] args) {
        String content = "hello_world";
        String encryptContent = encrypt(content);
        String decryptContent = decrypt(encryptContent);
        System.out.println(encryptContent);
        System.out.println(decryptContent);
    }

}
