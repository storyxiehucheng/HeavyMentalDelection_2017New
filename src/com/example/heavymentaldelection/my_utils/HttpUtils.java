package com.example.heavymentaldelection.my_utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * 网络请求
 * Created by story2 on 2017/11/26.
 */

public class HttpUtils {

    public static final String url_queryAll="http://192.168.8.128:8080/HeavyMentalDetection/queryAll.action";
    public static final String url_queryByDate="http://192.168.8.128:8080/HeavyMentalDetection/queryByDate.action";
    public static final String url_queryByCity="http://192.168.8.128:8080/HeavyMentalDetection/queryByCity.action";
    public static final String url_queryByPollution="http://192.168.8.128:8080/HeavyMentalDetection/queryByPollution.action";
    public static final String url_updateData="http://192.168.8.128:8080/HeavyMentalDetection/updateData.action";
    public static final String url_insertHeavyMental="http://192.168.8.128:8080/HeavyMentalDetection/insertHeavyMental.action";


    public static  final String URL_ERROR="url_error";
    public static  final String NET_ERROR="net_error";
    public static  final String REQUEST_ERROR="request_error";
    public static  final String PARAMETR_ERROR="parametr_error";




    /**通过post方式连接http网络
     * @param urlStr 需要连接的网络地址
     * @param jsonStr 需要发送的json格式的数据
     * @return 返回服务器返回的Json数据
     */
    public static String connectHttpByPost(String urlStr, String jsonStr)
    {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //设置输入流为true
            connection.setDoInput(true);
            //设置输出流为true
            connection.setDoOutput(true);
            //设置请求方式为post方式请求
            connection.setRequestMethod("POST");
            //设置超时连接时间
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            //关闭缓存，使用post方式请求的，必须关闭缓存
            connection.setUseCaches(false);
            //设置连接遵循重定向
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", String.valueOf(jsonStr.getBytes().length));
            connection.connect();

            DataOutputStream dop = new DataOutputStream(connection.getOutputStream());
            dop.write(jsonStr.getBytes());
            dop.flush();
            dop.close();

            int code=connection.getResponseCode();
            if(code==HttpURLConnection.HTTP_OK)
            {
                //请求成功,返回正确数据
                InputStream inputStream=connection.getInputStream();
                String streamToString = inputStreamToString(inputStream);
                Log.e("story","请求成功-----接收到的网络数据为："+streamToString);
                return streamToString;
            }
            else
            {
                //请求成功，但返回的错误提示
                InputStream inputStream=connection.getErrorStream();
                String streamToString = inputStreamToString(inputStream);
                Log.e("story","请求参数有误，错误数据为："+streamToString);
                return PARAMETR_ERROR;
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
            return REQUEST_ERROR;//请求方式错误
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return URL_ERROR;//网络地址错误
        } catch (IOException e) {
            e.printStackTrace();
            return NET_ERROR;//网络连接错误
        }
    }

    /**利用get方式请求http连接
     * @param url_path 要请求的网址
     * @return 返回请求后的数据
     */
    public static String connectHttpByGet(String url_path)
    {
        try {
            URL url = new URL(url_path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //设置超时连接时间为5秒
            connection.setConnectTimeout(5000);
            //设置转换为inputStream
            connection.setDoInput(true);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setReadTimeout(10000);
            //获取连接状态码
            int code = connection.getResponseCode();
            //判断状态码是否连接正常
            if (code == HttpURLConnection.HTTP_OK) {
                String toString = inputStreamToString(connection.getInputStream());
                Log.e("story", "请求成功，返回的数据为：" + toString);
                return toString;
            } else {
                //错误代码的时候，利用getErrorStream来获取流数据
                String message = connection.getResponseMessage();
                Log.e("story", "请求参数错误，返回的数据为" + message);
                InputStream inputStream = connection.getErrorStream();
                String streamToString = inputStreamToString(inputStream);
                Log.e("story", "请求参数错误，返回的数据为" + streamToString);
                return streamToString;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return URL_ERROR;
        } catch (IOException e) {
            e.printStackTrace();
            return NET_ERROR;
        }
    }

    /**将inputStream转换为String
     * @param inputStream 输入流inputStream
     * @return 返回转换后的String
     */
    private static String inputStreamToString(InputStream inputStream)
    {
        StringBuilder outBuffer=new StringBuilder();
        InputStreamReader inputStreamReader;
        BufferedReader bufferReader;
        String newLine;
        try {
            inputStreamReader=new InputStreamReader(inputStream,"UTF-8");
            bufferReader=new BufferedReader(inputStreamReader);
            while((newLine=bufferReader.readLine())!=null)
            {
                outBuffer.append(newLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return outBuffer.toString();
    }
}
