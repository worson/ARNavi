package com.haloai.hud.hudendpoint.arwaylib.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * author       : 龙;
 * date         : 2016/8/10;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.utils;
 * project_name : hudlauncher;
 */
public class FileUtils {
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);//png类型
        return baos.toByteArray();
    }

    // 写到sdcard中
    public static void write(byte[] bs, String fileDir, String filename) throws IOException {
        File file = new File(fileDir);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(fileDir + filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        out.write(bs);
        out.flush();
        out.close();
    }

    /**
     * 写文本到文件中
     * @param str
     * @param fileDir
     * @param filename
     */
    public static void write(String str,String fileDir,String filename){
        try {
            write(str.getBytes(),fileDir,filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
