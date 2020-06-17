package com.example.carl.optimizelistview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;

    private static final String url = "http://www.imooc.com/api/teacher?type=4&num=30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.lv_list);

        new MainAsyncTask().execute(url);

    }


    public List<NewsBean> getJsonData(String url){
        List<NewsBean> newsBeanList = new ArrayList<>();
        try {
            String jsonString = readStream(new URL(url).openStream());//利用readStream得到String数据
            Log.e("JSON",jsonString); //打印出string数据
            //下面解析得到的json数据
            JSONObject jsonObject;
            NewsBean newsBean;
            try {
                jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i=0; i<jsonArray.length(); i++){
                    jsonObject = jsonArray.getJSONObject(i);
                    newsBean = new NewsBean();
                    newsBean.newsIconUrl = jsonObject.getString("picSmall");
                    newsBean.newsTitle = jsonObject.getString("name");
                    newsBean.newsContent = jsonObject.getString("description");
                    newsBeanList.add(newsBean);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                log(e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            log(e.getMessage());
        }
        return newsBeanList;
    }

    //由输入流中读取数据并将数据返回
    public String readStream(InputStream in){
        InputStreamReader reader;
        String result = "";
        String line = "";
        try {
            reader = new InputStreamReader(in, "UTF-8");
            BufferedReader br = new BufferedReader(reader);
            while((line = br.readLine())!=null){
                result += line;
                log("result:"+result);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log("erro:"+e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            log("erro:"+e.getMessage());
        }
        return result;
    }


    //异步线程类
    class MainAsyncTask extends AsyncTask<String, Void, List<NewsBean>> {

        //该方法运行在后台线程中
        @Override
        protected List<NewsBean> doInBackground(String... params) {
            List<NewsBean> b= getJsonData(params[0]);
            log("b.size:"+b.size());
            return getJsonData(params[0]);
        }

        @Override
        protected void onPostExecute(List<NewsBean> newsBeanList) {
            super.onPostExecute(newsBeanList);
            log("newsBeanList.size:"+newsBeanList.size());
            NewsAdapter newsAdapter = new NewsAdapter(MainActivity.this, newsBeanList, listView);
            listView.setAdapter(newsAdapter);
        }
    }
    private void log(String str){
        Log.i("chen",str);
    }
}
