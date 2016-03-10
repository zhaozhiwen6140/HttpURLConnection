package com.example.cnlive.httpuriconnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button button;
    private TextView textView;
    private static final int GETMESSAGE=0;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case GETMESSAGE:
                    String response= (String) msg.obj;
                    textView.setText(response);
                    default:
                        break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        button=(Button)findViewById(R.id.button);
        textView=(TextView)findViewById(R.id.textView);
        button.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    @Override
    public void onClick(View v) {
       switch(v.getId()){
           case R.id.button:
               getMessageFromUrl();
               break;
           default:
               break;
       }
    }

    private void getMessageFromUrl() {
        new Thread(new Runnable() {
            HttpURLConnection connection=null;
            @Override
            public void run() {
                try {
                    URL url=new URL("http://www.baidu.com/");
                    try {
                        connection= (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
//                        connection.setRequestMethod("POST");
//                        DataOutputStream out=new DataOutputStream(connection.getOutputStream());
//                        out.writeBytes("username=admin&password=123456");//用post方法实现向服务器传送数据，写到服务器上。
//                       每条数据都需要以键值对的形式传送。数据与数据之间用&连接。
                        InputStream in=connection.getInputStream();
                        BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                        StringBuilder builder=new StringBuilder();
                        String line="";
                        if((line=reader.readLine())!=null){
                            builder.append(line);
                        }
                        parseXMLwithPull(builder.toString());
                        Message message=handler.obtainMessage();
                        message.what= GETMESSAGE;
                        message.obj=builder.toString();
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }finally {
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }

        }).start();
    }
    private void parseXMLwithPull(String s) {
        try {
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=factory.newPullParser();
            xmlPullParser.setInput(new StringReader(s));
            int eventType=xmlPullParser.getEventType();
//            获取当前解析到这个标签的类型
//            读取到xml的声明返回数字0 START_DOCUMENT;
//            读取到xml的结束返回数字1 END_DOCUMENT ;
//            读取到xml的开始标签返回数字2 START_TAG，就是起始标签被读取。
//            读取到xml的结束标签返回数字3 END_TAG，就是结束标签被读取。
            String id="";
            String name="";
            String version="";
            while(eventType!=XmlPullParser.END_DOCUMENT){//如果没到当前整个XML文件的末尾，就一直解析
                String nodeName=xmlPullParser.getName();
                switch (eventType){
                    case XmlPullParser.START_TAG://若起始标签被读取，那么会一直被读取下去，直到遇见结束标签。
                        if("id".equals(nodeName)){//此时的name会返回当前元素的名称，如id，name,version.
                            id=xmlPullParser.nextText();
                        }else if("name".equals(nodeName)){
                            name=xmlPullParser.nextText();
                        }else  if("version".equals(nodeName)){
                            version=xmlPullParser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if("app".equals(nodeName)){
                            Log.e("id",id);
                            Log.e("name",name);
                            Log.e("id",version);
                        }
                        break;
                    default:
                        break;

                }
                eventType=xmlPullParser.next();//在while中执行，执行完毕之后判断while（）中的条件是否是真的
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
