package com.lvjing.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * \* Created with IntelliJ IDEA.
 * \* User: 46512
 * \* Author: Alan
 * \* Date: 2018/6/1 22:32
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
@Service
public class GPSTranslate {

    // key
    private static final String KEY = "Z3IBZ-BSZCV-THCPX-UU4M3-MKAUS-3QFZB";

    /**
     * @Description: 通过经纬度获取位置
     * @Param: [log, lat]
     * @return: java.lang.String
     * @Author: zy
     * @Date: 2018/7/24
     */
    public static Map<String, Object> getLocation(String lng, String lat) {

        Map<String, Object> resultMap = new HashMap<String, Object>();

        // 参数解释：lng：经度，lat：维度。KEY：腾讯地图key
        String urlString = "http://apis.map.qq.com/ws/coord/v1/translate?locations=" +
                lat + "," + lng +"&type=1"+"&key=" + KEY ;



        String result = "";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            // 腾讯地图使用GET
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            // 获取地址解析结果
            while ((line = in.readLine()) != null) {
                result += line + "\n";
                //System.out.println(result);
            }
            in.close();
        } catch (Exception e) {
            e.getMessage();
        }

        // 转JSON格式
        JSONObject jsonObject = JSONObject.fromObject(result);
        //System.out.println(jsonObject);
        // 获取地址（行政区划信息） 包含有国籍，省份，城市
        JSONObject a = new JSONObject();
        //String code = jsonObject.getString("locations");
        String locations = jsonObject.get("locations").toString();
        //System.out.println(locations);

        JSONArray jsonArray = jsonObject.getJSONArray("locations");
        //System.out.println(jsonArray.getJSONObject(0).get("lat"));
        String lat_result = jsonArray.getJSONObject(0).get("lat").toString();
        String lng_result = jsonArray.getJSONObject(0).get("lng").toString();

        resultMap.put("lat", lat_result);
        resultMap.put("lng", lng_result);

        return resultMap;
    }

    //public static void main(String[] args) {
        // 测试
    //    String lat = "30.287574000";//维度
    //    String lng = "120.07875200";//经度

    //    Map<String, Object> map = getLocation(lng, lat);
    //    System.out.println(map);
    //    System.out.println("la：" + map.get("lat"));
    //    System.out.println("lo：" + map.get("lng"));
    //}
}