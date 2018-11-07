package com.example.thinkpad.myapplication;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thinkpad.myapplication.listener.ProgressListener;
import com.example.thinkpad.myapplication.other.ProgressRequestBody;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
 * 上传文件 报java.net.SocketException: sendto failed: EPIPE (Broken pipe)，这是因为服务器限制了大小，服务器主动把socket关闭了
 */
public class MainActivity extends AppCompatActivity {

    private TextView textView;
     // todo 这里需要根据自己的ip修改一下
    private String url = "http://192.168.2.58:8080/UploadFileDemo/MutilUploadServlet";
    private List<String> fileNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initUploadFile();
        initListener();
    }

    private void initListener() {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        upImage();
                    }
                }).start();
            }
        });
    }

    private void initView() {
        textView = (TextView) findViewById(R.id.textView);
    }

    //初始化上传文件的数据
    private void initUploadFile(){
        fileNames = new ArrayList<>();
        fileNames.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + "test.txt"); //txt文件
        fileNames.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + "bell.png"); //图片
        fileNames.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + "kobe.mp4"); //视频
        fileNames.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + "xinnian.mp3"); //音乐
    }

    private void upImage() {

         // 1.创建 OkHttpClient
        OkHttpClient mOkHttpClent = new OkHttpClient();

         // 2.创建 RequestBody
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (int i = 0; i < fileNames.size(); i++) { //对文件进行遍历
            File file = new File(fileNames.get(i)); //生成文件
            //根据文件的后缀名，获得文件类型
            String fileType = getMimeType(file.getName());
            builder.addFormDataPart( //给Builder添加上传的文件
                    "image",  //请求的名字，这个要和服务器预定？？
                    file.getName(), //文件的文字，服务器端用来解析的
                    RequestBody.create(MediaType.parse(fileType),file) //创建RequestBody，把上传的文件放入
            );
        }
        RequestBody requestBody = builder.build();

         // 3.创建 Request
        Request request = new Request.Builder()
                .url(url)
                .post(new ProgressRequestBody(requestBody, new ProgressListener() {
                    @Override
                    public void onProgress(long currentBytes, long contentLength, boolean done) {
                        Log.e("chris", "currentBytes:" + currentBytes);
                        Log.e("chris", "contentLength" + contentLength);
                        Log.e("chris", (100 * currentBytes) / contentLength + " % done ");
                        Log.e("chris", "done:" + done);
                        Log.e("chris", "================================");
                        //当前上传的进度值
                        int progress = (int) ((100 * currentBytes) / contentLength);
                    }
                }))
                .build();

         // 4.发起请求
        Call call = mOkHttpClent.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("chris", "onFailure: "+e );
                runOnUiThread(new Runnable() { // 子线程弹Toast
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "失败"+e, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("chris", "成功"+response);
                runOnUiThread(new Runnable() { // 子线程弹Toast
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }


    /**
     * 获取文件MimeType
     *
     * @param filename 文件名
     * @return
     */
    private static String getMimeType(String filename) {
        FileNameMap filenameMap = URLConnection.getFileNameMap();
        String contentType = filenameMap.getContentTypeFor(filename);
        if (contentType == null) {
            contentType = "application/octet-stream"; //* exe,所有的可执行程序
        }
        return contentType;
    }


}
