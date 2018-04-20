package com.example.heavymentaldelection.my_utils;

import android.util.Log;

import com.example.heavymentaldelection.global.MyGlobalStaticVar;

import java.util.ArrayList;
import java.util.Map;
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
            if(dataMap.get(keyTemp[i])-dataMap.get(keyTemp[i-1])>=0.01) temp[i-1]=1;
            else if(dataMap.get(keyTemp[i])-dataMap.get(keyTemp[i-1])<=-0.005) temp[i-1]=-1;
            else temp[i-1]=0;
//            temp[i-1]=dataMap.get(keyTemp[i])-dataMap.get(keyTemp[i-1]);
//            Log.e("story","处理后的临时数据temp为："+temp[i-1]);
        }
        for(int i=0;i<temp.length-1;i++)
        {
            if(temp[i]>0&&temp[i+1]<0)
            {
                resKeyList.add(keyTemp[i+1]);
            }
        }
        Log.e("story","查找到的key值集合为："+resKeyList);
        return resKeyList;
    }

    public static ArrayList<Integer> findExtremeMaxPoint_2(TreeMap<Integer,Double> dataMap)
    {
        ArrayList<Integer> resKeyList=new ArrayList<>();
        ArrayList<Integer> needKeyList=new ArrayList<>();
        ArrayList<Integer> deleteKeyList=new ArrayList<>();

        Log.e("story","数据滤出前的dataMap的大小"+dataMap.size());
        Set<Integer> doubleSet = dataMap.keySet();
        Integer[] keyTemp=new Integer[doubleSet.size()];
        keyTemp=doubleSet.toArray(keyTemp);
        int k=1;
        for(int i=1;i<keyTemp.length;i++)
        {
            if(dataMap.get(keyTemp[i])-dataMap.get(keyTemp[i-k])<0.05 && dataMap.get(keyTemp[i])-dataMap.get(keyTemp[i-k])>=0)
            {
                deleteKeyList.add(keyTemp[i-k]);
//                Log.e("story","--删除的key-1为："+keyTemp[i-k]);
                k=1;
            }
            else if(dataMap.get(keyTemp[i])-dataMap.get(keyTemp[i-k])>-0.05  && dataMap.get(keyTemp[i])-dataMap.get(keyTemp[i-k])<=0)
            {
                deleteKeyList.add(keyTemp[i]);
//                Log.e("story","--删除的key-2为："+keyTemp[i]);
                k++;
            }
            else
            {
                k=1;
            }
//            Log.e("story","k的值为："+k);
        }
        Log.e("story","--数据滤出后的dataMap的大小"+dataMap.size());
        double[] temp=new double[dataMap.size()];
        Set<Integer> keySet = dataMap.keySet();
        for (Integer aKeyTemp2 : keySet)
        {
            if (!deleteKeyList.contains(aKeyTemp2)) {
                needKeyList.add(aKeyTemp2);
            }
        }
        Log.e("story","--数据滤出后的key为："+needKeyList);

        for(int i=1;i<needKeyList.size();i++)
        {
            if(dataMap.get(needKeyList.get(i))-dataMap.get(needKeyList.get(i-1))>=0.02) temp[i-1]=1;
            else if(dataMap.get(needKeyList.get(i))-dataMap.get(needKeyList.get(i-1))<=-0.02) temp[i-1]=-1;
            else temp[i-1]=0;
        }

        for(int i=0;i<temp.length-1;i++)
        {
            if(temp[i]>0 && temp[i+1]<0)
            {
                resKeyList.add(needKeyList.get(i+1));
            }
        }
        Log.e("story","查找到的key值集合为："+resKeyList);
        return resKeyList;
    }

    /**
     * 根据给定的点做拟合直线
     * 利用最小二乘法，来拟合直线
     * https://wenku.baidu.com/view/9c0291e79b89680203d8259e.html
     * @param XData 为横坐标点集合
     * @param YData 为纵坐标点集合
     */
    public static void calibrationCurve(ArrayList<Integer> XData, ArrayList<Double> YData)
    {
        //1、计算总数量
        int n=XData.size();
//        Log.e("story","计算的n为："+n);
        //2、求x的平均值
        double sumX=0;
        for (int x:XData)
        {
            sumX += x;
        }
//        Log.e("story","计算的sumX为："+sumX);
        double averageX=sumX/n;
//        Log.e("story","计算的averageX为："+averageX);
        //3、求y的平均值
        double sumY=0;
        for(double y:YData)
        {
            sumY += y;
        }
        double averageY=sumY/n;
//        Log.e("story","计算的averageY为："+averageY);
        //4、求lxx
        double sumXX=0;
        for(int x: XData)
        {
            sumXX += x*x;
        }
        double lxx=sumXX - n * averageX * averageX;
//        Log.e("story","计算的lxx为："+lxx);
        //5、求lxy
        double sumXY=0;
        for(int i=0;i<n;i++)
        {
            sumXY += XData.get(i) * YData.get(i);
        }
        double lxy= sumXY - n * averageX * averageY;
//        Log.e("story","计算的lxy为："+lxy);
        //6、求斜率参数a
        double slope=lxy/lxx;
        //7、求截距参数b
        double intercept=averageY-averageX*slope;

        MyGlobalStaticVar.Curve_Slope=slope;//记录斜率
        MyGlobalStaticVar.Curve_Intercept=intercept;//记录截距
        MyGlobalStaticVar.isCurveFitting=true;
        Log.e("story","所求的线性拟合直线为：y= "+slope+"x + "+intercept);
    }

    /**
     * 根据给定的点做拟合直线
     * 利用最小二乘法，来拟合直线
     * https://wenku.baidu.com/view/9c0291e79b89680203d8259e.html
     * @param curMap 包含横纵坐标的点的map集合，横坐标为键，纵坐标为值
     *
     */
    public static void calibrationCurve(Map<Integer,Double> curMap)
    {
        Set<Integer> XData = curMap.keySet();
        //1、计算总数量
        int n=XData.size();
//       Log.e("story","计算的n为："+n);
        //2、求x和y的平均值
        double sumX=0;
        double sumY=0;
        for (int x:XData)
        {
            sumX += x;
            sumY +=curMap.get(x);
        }
//        Log.e("story","计算的sumX为："+sumX);
        double averageX=sumX/n;
        double averageY=sumY/n;
//        Log.e("story","计算的averageX为："+averageX);
//        Log.e("story","计算的averageY为："+averageY);
        //4、求lxx
        double sumXX=0;
        for(int x: XData)
        {
            sumXX += x*x;
        }
        double lxx=sumXX - n * averageX * averageX;
//        Log.e("story","计算的lxx为："+lxx);
        //5、求lxy
        double sumXY=0;
        for(int x:XData)
        {
            sumXY += x * curMap.get(x);
        }
        double lxy= sumXY - n * averageX * averageY;
//        Log.e("story","计算的lxy为："+lxy);
        //6、求斜率参数a
        double slope=lxy/lxx;
        //7、求截距参数b
        double intercept=averageY-averageX*slope;

        MyGlobalStaticVar.Curve_Slope=slope;//记录斜率
        MyGlobalStaticVar.Curve_Intercept=intercept;//记录截距
        MyGlobalStaticVar.isCurveFitting=true;
        Log.e("story","所求的线性拟合直线为：y= "+slope+"x + "+intercept);
    }
}
