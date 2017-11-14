package com.tianbao.mi.utils;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * json
 * Created by edianzu on 2017/11/13.
 */
public class JsonEncloseUtils {

    /**
     * 将对象分装为 json 字符串
     */
    public static Object jsonEnclose(Object obj) {
        try {
            if (obj instanceof Map) {   // 如果是 Map 则转换为 JsonObject
                Map<String, Object> map = (Map<String, Object>) obj;
                Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
                JSONStringer jsonStringer = new JSONStringer().object();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> entry = iterator.next();
                    jsonStringer.key(entry.getKey()).value(jsonEnclose(entry.getValue()));
                }
                JSONObject jsonObject = new JSONObject(new JSONTokener(jsonStringer.endObject().toString()));
                return jsonObject;
            } else if (obj instanceof List) {  // 如果是 List 则转换为 JsonArray
                List<Object> list = (List<Object>) obj;
                JSONStringer jsonStringer = new JSONStringer().array();
                for (int i = 0; i < list.size(); i++) {
                    jsonStringer.value(jsonEnclose(list.get(i)));
                }
                return new JSONArray(new JSONTokener(jsonStringer.endArray().toString()));
            } else {
                return obj;
            }
        } catch (Exception e) {
            Log.e("jsonUtil--Enclose", e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * Object 转成 String
     */
    public static String btToString(Object msg) {
        return new Gson().toJson(msg);
    }
}
