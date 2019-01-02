package com.ai.mexxen.tcaitest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.os.Environment;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;
import android.os.Message;
import android.os.Handler;


import cn.xsshome.taip.util.FileUtil;
import cn.xsshome.taip.ocr.TAipOcr;
import cn.xsshome.taip.nlp.TAipNlp;
import cn.xsshome.taip.util.ImageDispose;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.widget.Toast.*;

public class MainActivity extends AppCompatActivity {
    private Button mButtonIdDetect, mButtonChat,mButtonIdOcr;
    private TextView mTextView;
    private EditText mTextChat;
    public int REQUEST_CODE_TAKE_PICTURE = 1;
    public Bitmap bm;
    public int iAction = 0;

    //设置AppID/AppKey
    public static final String AppID = "2109086611";
    public static final String AppKey = "dZACwRVu2ZBQ9HBa";

    // 初始化一个TAipFace
    TAipOcr OcrClient = new TAipOcr(AppID,AppKey);
    TAipNlp NlpClient = new TAipNlp(AppID,AppKey);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initUI();
    }

    public void initUI() {
        mButtonIdDetect = (Button) findViewById(R.id.iddetect);
        mButtonChat = (Button) findViewById(R.id.chat);
        mButtonIdOcr = (Button)findViewById(R.id.idocr);
        mTextView = (TextView)findViewById(R.id.textView);
        mTextChat = (EditText)findViewById(R.id.editText);

        mButtonIdDetect.setOnClickListener(mButtonListener);
        mButtonIdOcr.setOnClickListener(mButtonListener);
        mButtonChat.setOnClickListener(mButtonListener);
        mTextView.setText("This is a test");
        mTextChat.setText("上海天气");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    String strTemp = "";
    String strResult = "";

    private Handler  handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                mTextView.setText(strResult); //View.ininvalidate()
                mTextChat.setText(strResult);
                //sendEmptyMessageDelayed(0, 1000);
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if ( resultCode == RESULT_OK) {
                    bm = (Bitmap) data.getExtras().get("data");
                    //savePath = FileUtil.saveBitmap(bm);
                    //img.setImageBitmap(bm);
                }

                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        //View cv = getWindow().getDecorView();
                        if(iAction == 1) {
                            try {
                                strTemp = OcrClient.idcardOcr(ImageDispose.Bitmap2Bytes(bm), 0);//身份证正面(图片)识别;
                                try {
                                    String sTemp = "";
                                    JSONObject jsonObject = new JSONObject(strTemp);//jsonArray.getJSONObject(0);
                                    //注意：results中的内容带有中括号[]，所以要转化为JSONArray类型的对象
                                    String result = jsonObject.optString("data");
                                    JSONObject jsonObject1 = new JSONObject(result);
                                    sTemp = sTemp + "Name: " + jsonObject1.optString("name") + "\n";
                                    sTemp = sTemp + "Sex: " + jsonObject1.optString("sex") + "\n";
                                    sTemp = sTemp + "Nation: " + jsonObject1.optString("nation") + "\n";
                                    sTemp = sTemp + "Birth: " + jsonObject1.optString("birth") + "\n";
                                    sTemp = sTemp + "Address: " + jsonObject1.optString("address") + "\n";
                                    sTemp = sTemp + "ID: " + jsonObject1.optString("id");
                                    strResult = sTemp;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if(iAction == 2)
                        {
                            try {
                                strTemp = OcrClient.generalOcr(ImageDispose.Bitmap2Bytes(bm));//ocr
                                try {
                                    String sTemp = "";
                                    JSONObject jsonObject = new JSONObject(strTemp);//jsonArray.getJSONObject(0);
                                    //注意：results中的内容带有中括号[]，所以要转化为JSONArray类型的对象
                                    String result = jsonObject.optString("data");
                                    JSONObject jsonObject1 = new JSONObject(result);
                                    String result1 =jsonObject1.optString("item_list");

                                    JSONArray jsonArray = new JSONArray(result1);
                                    int iLength = jsonArray.length();
                                    for (int i=0; i < jsonArray.length(); i++)    {
                                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                        sTemp = sTemp + jsonObject2.getString("itemstring") + "\n";
                                        strResult = sTemp;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                while(strResult == "") {
                    try {
                        Thread.sleep(200);
                    } catch (Exception e) {

                    }
                }
                handler.sendEmptyMessageDelayed(0, 100);
                break;
        }
    }
    OnClickListener mButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mTextView.setText("查询中...");
            switch (v.getId()) {
                case R.id.chat:
                    // Android 4.0 之后不能在主线程中请求HTTP请求
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try {
                                strTemp = wechat(NlpClient);
                                try{
                                    JSONObject jsonObject = new JSONObject(strTemp);//jsonArray.getJSONObject(0);
                                    //注意：results中的内容带有中括号[]，所以要转化为JSONArray类型的对象
                                    String result = jsonObject.optString("data");
                                    JSONObject jsonObject1 = new JSONObject(result);
                                    strResult = jsonObject1.optString("answer");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    while(strResult == "") {
                        try {
                            Thread.sleep(200);
                        } catch (Exception e) {

                        }
                    }
                    handler.sendEmptyMessageDelayed(0, 100);
                    break;
                case R.id.iddetect:
                    iAction = 1; //id card
                case R.id.idocr:
                    iAction = 2; //common ocr
                default:
                    Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //系统常量， 启动相机的关键
                    startActivityForResult(openCameraIntent, REQUEST_CODE_TAKE_PICTURE); // 参数常量为自定义的request code, 在取返回结果时有用
                    break;
            }
        }
    };

    public static String iddetect(TAipOcr client) throws Exception {
        String result = "";

        /*
        //参数为本地图片路径
        String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Camera/1.jpg";// +"/DCIM/1.jpg";//  /storage/sdcard0/DCIM/1.jpg

        //String result = client.idcardOcr(imagePath, 0);//身份证正面(图片)识别;
        //String result = client.idcardOcr("./idcard2.jpg", 1);//身份证反面(国徽)识别;
        //参数为本地图片二进制数组

        byte[] image = FileUtil.readFileByBytes(imagePath);
        result = client.idcardOcr(image , 0);//身份证正面(图片)识别;
        //String result = client.idcardOcr(image , 1);//身份证反面(国徽)识别;
        */

        return result;
    }

    public String wechat(TAipNlp client) throws Exception {
        //参数内容
        String text = mTextChat.getText().toString();
        String session = new Date().getTime()/1000+"";//会话标识（应用内唯一）
        //String result = client.nlpTextchat(text);//基础闲聊
        String result = client.nlpTextchat(session,text);//基础闲聊
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
