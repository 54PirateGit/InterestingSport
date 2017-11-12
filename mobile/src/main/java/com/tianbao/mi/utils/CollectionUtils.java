package com.tianbao.mi.utils;

import com.tianbao.mi.bean.PartnerBean;

import java.util.ArrayList;
import java.util.Map;

/**
 * Collections
 * Created by edianzu on 2017/11/5.
 */
public class CollectionUtils {

    /**
     * Map to List
     * @param map 数据源
     * @return List
     */
    public static ArrayList<PartnerBean> mapToList(Map<String, Map<String, String>> map) {
        if (map == null || map.size() <= 0) return null;
        ArrayList<PartnerBean> list = new ArrayList<>();
        PartnerBean bean;
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            bean = new PartnerBean();
            bean.setKey(entry.getKey());
            Map<String, String> m = entry.getValue();
            bean.setHead(m.get("avatar"));
            bean.setNick(m.get("nick"));
            list.add(bean);
        }
        return list;
    }
}
