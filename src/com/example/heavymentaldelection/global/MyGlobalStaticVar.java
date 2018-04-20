package com.example.heavymentaldelection.global;

/**
 * Created by story2 on 2017/11/20.
 * 全局静态变量
 */

public class MyGlobalStaticVar {

    /**
     * 记录蓝牙是否打开
     */
    public static Boolean isBleOpen=false;

    /**
     * 记录拟合直线的斜率
     */
    public static double Curve_Slope=0.0;

    /**
     * 记录拟合直线的截距
     */
    public static double Curve_Intercept=0.0;

    /**
     * 记录曲线是否已经拟合成功
     */
    public static boolean isCurveFitting=false;
}
