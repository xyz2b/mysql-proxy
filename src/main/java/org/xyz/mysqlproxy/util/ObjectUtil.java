package org.xyz.mysqlproxy.util;

import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import java.io.*;

// json工具，编码使用json，解码使用fastjson
public class ObjectUtil {
    public static byte[] Object2JsonBytes(Object obj) {
        // 尽量把对象转换成JSON保存更稳妥
        String json = ObjectToJson(obj);
        try {
            return json.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T JsonBytes2Object(byte[] bytes, Class<T> tClass) {
        // 尽量把对象转换成JSON保存更稳妥
        try {
            String json = new String(bytes, "UTF-8");
            T t = JsonToObject(json, tClass);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 使用谷歌Gson转成对象转成json字符串
    public static String ObjectToJson(Object obj) {
        String json = new Gson().toJson(obj);
        return json;
    }

    // 使用阿里fastjson将json字符串转成对象
    public static <T> T JsonToObject(String json, Class<T> tClass) {
        T t = JSON.parseObject(json, tClass);
        return t;
    }

    public static byte[] ObjectToByte(Object obj) {
        byte[] bytes = null;
        try {
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);

            bytes = bo.toByteArray();

            bo.close();
            oo.close();
        } catch (Exception e) {
            System.out.println("translation" + e.getMessage());
            e.printStackTrace();
        }
        return bytes;
    }


    public static Object ByteToObject(byte[] bytes) {
        Object obj = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);

            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            System.out.println("translation" + e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }
}
