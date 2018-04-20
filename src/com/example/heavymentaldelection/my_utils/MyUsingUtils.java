package com.example.heavymentaldelection.my_utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 常用的一些工具
 * Created by story2 on 2017/11/27.
 */

public class MyUsingUtils {

    public static String DateToString(Date date)
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return sdf.format(date);
    }

    public static double averageList(List<Double> list)
    {
        double sum=0;
        for(double d:list)
        {
            sum+=d;
        }
        DecimalFormat df=new DecimalFormat("#.0000");
        String format = df.format(sum/list.size());
        return Double.parseDouble(format);
    }

    /**
     * 文件存储
     * @param fileName  需要存储的文件名
     * @param fileData  需要存储的文件
     * @param fileDir   需要存储的文件目录
     */
    public static void DataSaveToFile(String fileDir,String fileName,ArrayList<String> fileData)
    {
        if(fileData==null) return;
        Log.e("story", "准备存储数据");
        String EnvironmentPath= Environment.getExternalStorageDirectory().getAbsoluteFile().getPath();
        File file=new File(EnvironmentPath+File.separator+fileDir);
        if(!file.exists())
        {
            boolean b = file.mkdirs();
            Log.e("story","文件目录是否创建成功："+b);
        }
        else
        {
            Log.e("story","文件目录已经存在:"+file.getAbsolutePath());
        }
        String path=file.getAbsolutePath()+File.separator+fileName+".txt";
        Log.e("story","文件path:"+path);
        file=new File(path);
        if(!file.exists())
        {
            try {
                boolean newFile = file.createNewFile();
                Log.e("story","文件是否创建成功:"+newFile);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("story","文件创建失败:"+e.toString());
                return;
            }
        }
        FileWriter fw=null;
        BufferedWriter bufferedWriter=null;
        try {
            fw = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fw);
            for (String double1 : fileData) {
                String Cstring=double1.replaceAll("#", "\t");
                bufferedWriter.write(Cstring);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            Log.e("story", "数据已经储存完毕");
        } catch (IOException e) {
            Log.e("story", "数据存储失败："+e.toString());
        } finally
        {
            if(fw!=null)
            {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bufferedWriter!=null)
            {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
