package com.example.heavymentaldelection.my_utils;

import com.example.heavymentaldelection.Info.HeavyMentalDataInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 从服务器查询数据
 * Created by story2 on 2017/11/27.
 */

public class QueryFromNet {
    /**
     * 查询全部数据
     * @return 返回查询后的json格式的字符串，或者错误信息
     */
    public static String queryAll()
    {
        return HttpUtils.connectHttpByGet(HttpUtils.url_queryAll);
    }

    /**按照污染程度来查询
     * @param pollution 需要查询的污染程度
     * @return 返回json格式的数据或者错误信息
     */
    public static String queryByPollution(String pollution) throws JSONException
    {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("pollution",pollution);
        return HttpUtils.connectHttpByPost(HttpUtils.url_queryByPollution,jsonObject.toString());
    }

    /**按照城市来查询
     * @param city 需要查询的城市的值
     * @return 返回json格式的数据或者错误信息
     */
    public static String queryBycity(String city) throws JSONException
    {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("city",city);
        return HttpUtils.connectHttpByPost(HttpUtils.url_queryByCity,jsonObject.toString());
    }

    /**按照日期来查询
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 返回json格式的数据或者错误信息
     * @throws JSONException
     */
    public static String queryByDate(String startDate, String endDate) throws JSONException {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("startDate",startDate);
        jsonObject.put("endDate",endDate);
        return HttpUtils.connectHttpByPost(HttpUtils.url_queryByDate,jsonObject.toString());
    }

    /**插入一条新数据
     * @param hmInfo 需要插入的对象
     * @return 成功返回success
     */
    public static String insertHeavyMental(HeavyMentalDataInfo hmInfo)
    {
        Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String jsonStr = gson.toJson(hmInfo);
       return  HttpUtils.connectHttpByPost(HttpUtils.url_insertHeavyMental,jsonStr);
    }

    /**更新一条新数据，根据ID来更新
     * @param hmInfo 需要更新的对象
     * @return 成功返回success
     */
    public static String updateHeavyMentalData(HeavyMentalDataInfo hmInfo)
    {
        Gson gson=new Gson();
        String jsonStr = gson.toJson(hmInfo);
        return  HttpUtils.connectHttpByPost(HttpUtils.url_updateData,jsonStr);
    }
}
