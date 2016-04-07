package com.ivy.icrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ivy on 2016/3/23.
 */
public class FileUtils {
    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        InputStream inStream = null;
        FileOutputStream fs = null;
        if (!createFile(new File(newPath)))
            return false;
        try {
            int byteread;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                inStream = new FileInputStream(oldPath); //读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];

                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inStream != null)
                    inStream.close();
                if (fs != null) {
                    fs.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public static boolean createFile(String path) {
        return createFile(new File(path));
    }

    /**
     * 根据全路径创建文件夹,会自动根据/判断文件夹是否存在，不存在，则自动创建
     */
    public static boolean createFile(File file) {
        System.out.println("-------path:" + file.getAbsolutePath());
        if (file.exists()) {
            return true;
        } else {
            if (file.isDirectory()) {
                return file.mkdirs();
            } else {
                if (!file.getParentFile().exists()) {
                    boolean isCreate = file.getParentFile().mkdirs();
                    if (!isCreate) {
                        return false;
                    }
                }
                try {
                    file.createNewFile();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

            }
        }
    }
}
