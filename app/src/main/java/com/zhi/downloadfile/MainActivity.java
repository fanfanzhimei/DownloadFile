package com.zhi.downloadfile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zhi.utils.DownloadHelper;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener{

    private static final int MESSAGE_SUCCESS = 0x1;
    private static final int MESSAGE_FAIL = 0x2;

    private static final String path = "http://192.168.1.3:8080/FileUpload/kecheng.avi";

    private EditText mEtPath;
    private Button mBtnDownload;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_SUCCESS:
                    Toast.makeText(MainActivity.this,"下载成功",Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_FAIL:
                    Toast.makeText(MainActivity.this,"下载失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        mEtPath.setText(path);
        mBtnDownload.setOnClickListener(this);
    }

    private void initViews() {
        mEtPath = (EditText) findViewById(R.id.et_path);
        mBtnDownload = (Button) findViewById(R.id.btn_download);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_download:
                download();
                break;
        }
    }

    private void download(){
        new Thread(){
            @Override
            public void run() {
                try {
                    DownloadHelper downloadUtils = DownloadHelper.getInstance();
                    boolean success = downloadUtils.download(path, 3);
                    if(success){
                        mHandler.sendEmptyMessage(MESSAGE_SUCCESS);
                    } else {
                        mHandler.sendEmptyMessage(MESSAGE_FAIL);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("DownloadThread:", e.toString());
                    mHandler.sendEmptyMessage(MESSAGE_FAIL);
                }
            }
        }.start();
    }
}
