package com.zhi.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/10/25.
 */
public class DownloadHelper {
    private static class DownloadHelp{
        private static final DownloadHelper INSTANCE =new DownloadHelper();
    }
    private DownloadHelper(){
    }
    public static final DownloadHelper getInstance(){
        return  DownloadHelp.INSTANCE;
    }

    /**
     * @param path  目标文件的下载地址
     * @param threadNum   开启线程的总条数
     * @throws IOException
     */
    public boolean download(String path, int threadNum) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        int length = conn.getContentLength();
        String fileName = createLocalFile(path, length);
        int state = conn.getResponseCode();
        if(200 == state){
            for(int threadId = 0; threadId < threadNum; threadId++){
                new DownloadThread(path, fileName, threadId, threadNum, length).start();
            }
            return true;
        }
        return false;
    }
    /**
     * 在本地生成同样大小的文件，用来存放下载的数据
     * @param path  目标下载地址
     * @param length  目标文件的中长度
     * @return 生成的本地文件的文件名称
     * @throws FileNotFoundException  目标文件不存在
     * @throws IOException
     */
    private String createLocalFile(String path, int length) throws FileNotFoundException, IOException {
        String fileName = path.substring(path.lastIndexOf("/")+1);

        File file = checkLocalFile(fileName);
        RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
        accessFile.setLength(length);
        accessFile.close();
        return fileName;
    }

    public File checkLocalFile(String fileName) throws IOException {
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(rootPath, fileName);
        if(!file.exists()){
            file.createNewFile();
        }
        return file;
    }
}

class DownloadThread extends Thread{

    private String path;  // 要下载的目标地址
    private String fileName;  // 本地存放文件名
    private int threadId; // 第几条线程
    private int threadNum; // 第几条线程
    private int length;  // 总数据的长度��

    public DownloadThread(String path, String fileName, int threadId, int threadNum, int length) {
        this.path = path;
        this.fileName = fileName;
        this.threadId = threadId;
        this.threadNum = threadNum;
        this.length = length;
    }

    public void run(){
        int block = length%threadNum == 0? length/threadNum : length/threadNum+1; // 每一块的数据量
        int start = threadId * block; // 每一个线程的开始位置
        int end = (threadId+1)*block-1; // 每一个线程的结束位置�
        try {
            File file = DownloadHelper.getInstance().checkLocalFile(fileName);
            RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
            accessFile.seek(start);  // 将线程中的下载位置赋值给RandomAccessFile(seek类似指针位置)
            HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            int state = conn.getResponseCode();
            if(200 == state){
                InputStream in = conn.getInputStream();
                byte[] bytes = new byte[1024];
                int len = 0;
                while((len = in.read(bytes)) != -1){
                    accessFile.write(bytes, 0 , len);
                }
                Log.e("DownloadThread:", "第" + (threadId + 1) + "条线程执行完毕");
            }
            accessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("DownloadThread:", e.toString());
        }
    }
}