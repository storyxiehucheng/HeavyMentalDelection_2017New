package com.example.heavymentaldelection.my_utils;

import android.util.Log;

import org.xmlpull.v1.XmlSerializer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

/**
 * 接收的数据分析
 * Created by story2 on 2017/11/21.
 */

public class MyDataAnalysis {

    /**
     * 查找极大值点
     * @param dataMap 需要的查询的保留了电流电压值，且集合顺序是按照电压增加的顺序集合
     * @return 返回查询到的数据map中的key的集合值
     */
    public static ArrayList<Integer> findExtremeMaxPoint(TreeMap<Integer,Double> dataMap)
    {
        ArrayList<Integer> resKeyList=new ArrayList<>();
        double[] temp=new double[dataMap.size()];
        Set<Integer> doubleSet = dataMap.keySet();
        Integer[] keyTemp=new Integer[doubleSet.size()];
        keyTemp=doubleSet.toArray(keyTemp);
        for(int i=1;i<keyTemp.length;i++)
        {
            temp[i-1]=dataMap.get(keyTemp[i])-dataMap.get(keyTemp[i-1]);
            Log.e("story","处理后的临时数据temp为："+temp[i-1]);
        }
        for(int i=0;i<temp.length-1;i++)
        {
            if(temp[i]>=0&&temp[i+1]<0)
            {
                resKeyList.add(keyTemp[i+1]);
            }
        }
        Log.e("story","查找到的key值集合为："+resKeyList);
        return resKeyList;
    }

    /**
     * 根据给定的点做拟合直线
     * 利用最小二乘法，来拟合直线
     * https://wenku.baidu.com/view/9c0291e79b89680203d8259e.html
     * @param Xdata 为横坐标点集合
     * @param Ydata 为纵坐标点集合
     */
    public static void calibrationCurve(ArrayList<Integer> Xdata, ArrayList<Double> Ydata)
    {
        //1、计算总数量
        int n=Xdata.size();
//        Log.e("story","计算的n为："+n);
        //2、求x的平均值
        double sumX=0;
        for (int x:Xdata)
        {
            sumX += x;
        }
//        Log.e("story","计算的sumX为："+sumX);
        double averageX=sumX/n;
//        Log.e("story","计算的averageX为："+averageX);
        //3、求y的平均值
        double sumY=0;
        for(double y:Ydata)
        {
            sumY += y;
        }
        double averageY=sumY/n;
//        Log.e("story","计算的averageY为："+averageY);
        //4、求lxx
        double sumXX=0;
        for(int x: Xdata)
        {
            sumXX += x*x;
        }
        double lxx=sumXX - n * averageX * averageX;
//        Log.e("story","计算的lxx为："+lxx);
        //5、求lxy
        double sumXY=0;
        for(int i=0;i<n;i++)
        {
            sumXY += Xdata.get(i) * Ydata.get(i);
        }
        double lxy= sumXY - n * averageX * averageY;
//        Log.e("story","计算的lxy为："+lxy);
        //6、求斜率参数a
        double slope=lxy/lxx;
        //7、求截距参数b
        double intercept=averageY-averageX*slope;

        MyGlobalStaticVar.Curve_Slope=slope;//记录斜率
        MyGlobalStaticVar.Curve_Intercept=intercept;//记录截距
        Log.e("story","所求的线性拟合直线为：y= "+slope+"x + "+intercept);
    }
}
