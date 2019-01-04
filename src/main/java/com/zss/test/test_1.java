package com.zss.test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class test_1 {
	private static final String KEY_MD5 = "MD5";
	private static final String[] strDigits = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
	public static void main(String[] args) throws ParseException, NoSuchAlgorithmException {
		SimpleDateFormat sft = new SimpleDateFormat();
		System.out.println(sft.format(new Date()));
		MessageDigest md = MessageDigest.getInstance(KEY_MD5);
		 String body = "\"aa\":{\"bb\":\"\",\"cc\": \"\",\"dd\": \"\"}\"";
         System.out.println(body);
		 System.out.println(byteToString(md.digest(body.getBytes())));
	}
	// 返回形式为数字跟字符串
    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if (iRet < 0) {
            iRet += 256;
        }
        //System.out.println("iRet:"+iRet);
        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
       // System.out.println("iD1:"+iD1);
        //System.out.println("iD2:"+iD2);
        return strDigits[iD1] + strDigits[iD2];
    }

    // 转换字节数组为16进制字串
    private static String byteToString(byte[] bByte) {
        StringBuffer sBuffer = new StringBuffer();
        for (int i = 0; i < bByte.length; i++) {
        	System.out.println(bByte[i]);
            sBuffer.append(byteToArrayString(bByte[i]));
            System.out.println("byteToString:"+byteToArrayString(bByte[i]));
        }
        return sBuffer.toString();
    }
    /**
     * MD5加密
     * @param strObj
     * @return
     * @throws Exception
     */
    public static String GetMD5Code(String strObj) throws Exception{
        MessageDigest md = MessageDigest.getInstance(KEY_MD5);
        // md.digest() 该函数返回值为存放哈希值结果的byte数组
        return byteToString(md.digest(strObj.getBytes()));
    }

}
