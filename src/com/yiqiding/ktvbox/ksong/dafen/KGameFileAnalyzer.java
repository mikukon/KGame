package com.yiqiding.ktvbox.ksong.dafen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.util.Log;

import com.yiqiding.ktvbox.R;

/**
 * Created by so898 on 14-7-21.
 */
public class KGameFileAnalyzer {
    public static KGameFileInfo getGameFileFromServer(String path){
        URL url;
        InputStream is = null;
        BufferedReader br;
        KGameFileInfo fileInfo = null;
        try {
            url = new URL(path);
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));
            fileInfo = new KGameFileInfo(br);
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {}
        }
        return fileInfo;
    }

    public static KGameFileInfo getGameFileFromLocal(String path){
        BufferedReader br = null;
        KGameFileInfo fileInfo = null;
        try {
            br = new BufferedReader(new FileReader(path));
            fileInfo = new KGameFileInfo(br);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {}
        }
        return fileInfo;
    }
    
    public static List<KgBcInfo> getKgBcInfoFileFromLocal(Resources resources){
    	InputStream in = resources.openRawResource(R.raw.kg_algorithm);
    	List<KgBcInfo> bcList = new ArrayList<KgBcInfo>();
    	BufferedReader br = null;
        try {
        //    br = new BufferedReader(new FileReader(resources));
        	br = new BufferedReader(new InputStreamReader(in));
        	String line = "";
			while ((line = br.readLine()) != null) {
				KgBcInfo kgBcInfo = new KgBcInfo() ;
				String[] arr = line.split(",");
				if(arr.length!=2){
					continue;
				}
				kgBcInfo.setRes(Float.valueOf(arr[1]));
				String[] temp = arr[0].split("-");
				kgBcInfo.setStart(Float.valueOf(temp[0]));
				if("*".equals(temp[1])){
					kgBcInfo.setEnd(Float.POSITIVE_INFINITY);
				}else{
					kgBcInfo.setEnd(Float.valueOf(temp[1]));
				}
				bcList.add(kgBcInfo);
			}
        } catch (IOException e) {
        	Log.i("KGameFileAnalyzer", "read kgAlgorithm failed");
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
                if (in != null)in.close();
            } catch (IOException ex) {}
        }
        return bcList;
    }
}
