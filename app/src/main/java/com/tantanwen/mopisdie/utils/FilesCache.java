package com.tantanwen.mopisdie.utils;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dan on 2015/8/1.
 */
public class FilesCache {

    private String fileName;
    private Context mContext;
    private String stream;

    public FilesCache(Context mContext) {
        this.mContext = mContext;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    public void setStream(String stream){
        this.stream = stream;
    }

    /*
    读取
     */
    public String load(){

        FileInputStream inStream;
        if(this.fileName == null){
            return null;
        }

        try {
            inStream = mContext.openFileInput(this.fileName);
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            byte[] buffer=new byte[1024];
            int length=-1;
            while((length=inStream.read(buffer))!=-1)   {
                stream.write(buffer,0,length);
            }
            stream.close();
            inStream.close();
            return stream.toString();

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /*
     写入
     */
    public boolean save(){

        if(this.fileName == null || this.stream == null){
            return false;
        }
        try {
            FileOutputStream outStream = mContext.openFileOutput(this.fileName, Context.MODE_PRIVATE);
            outStream.write(this.stream.getBytes());
            outStream.close();

        } catch (FileNotFoundException e) {
            return false;
        }catch (IOException e) {
            return false;
        }
        return true;
    }
}
