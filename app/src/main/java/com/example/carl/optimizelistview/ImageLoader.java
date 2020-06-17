package com.example.carl.optimizelistview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ImageLoader {

    private ImageView mImageView;
    private String mUrl;

    private LruCache<String, Bitmap> mCaches;
    private ListView mListView;
    private Set<NewsAsyncTask> mAsyncTask;

    public ImageLoader(ListView listView){

        mListView = listView;
        mAsyncTask = new HashSet<>();

        //下面是建立缓存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();  //运行时最大内存
        int cacheSize = maxMemory/4;
        mCaches = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    //将bitmap添加到缓存
    public void addBitmapToCache(String url,Bitmap bitmap){
        if (getBitmapFormCache(url) == null){
            mCaches.put(url, bitmap);
        }
    }
    //从缓存中获取数据
    public Bitmap getBitmapFormCache(String url){
        return mCaches.get(url);
    }
    //===================================下面为普通异步加载===========================================
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mImageView.getTag().equals(mUrl)) { //当url标记和原先设置的一样时，才设置ImageView
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    public void showImageByThread(ImageView imageView, final String url) {

        this.mImageView = imageView;
        this.mUrl = url;
        new Thread() {
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFormURL(url);
                Message message = Message.obtain();
                message.obj = bitmap;
                handler.sendMessage(message);
            }
        }.start();
    }

    //====================上面是使用普通的异步加载，下面是使用AsyncTask进行的异步加载==================
    public Bitmap getBitmapFormURL(String urlString) {
        Bitmap bitmap;
        InputStream inputStream = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            inputStream = new BufferedInputStream(conn.getInputStream());  //得到图片的数据流
            bitmap = BitmapFactory.decodeStream(inputStream);  //根据数据流来解析出图片的bitmap
            conn.disconnect();
            return bitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }


    //加载图片
    public void showImageByAsyncTask(ImageView ImageView, String url) {
        Bitmap bitmap = getBitmapFormCache(url);
        if (bitmap == null){
            ImageView.setImageResource(R.mipmap.ic_launcher);
        }else{
            ImageView.setImageBitmap(bitmap);
        }
    }

    public void cancelAllTasks(){
        if (mAsyncTask != null){
            for (NewsAsyncTask task : mAsyncTask){
                task.cancel(false);
            }
        }
    }

    public void loadImages(int start, int end){
        for (int i = start; i < end; i++){
            String url = NewsAdapter.URLS[i];
            //由缓存中得到bitmap
            Bitmap bitmap = getBitmapFormCache(url);
            if (bitmap == null){
                //当bitmap为空时，由AsyncTask进行加载，并在onPostExecute()方法中setImageBitmap
                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mAsyncTask.add(task);
            } else {
                //当bitmap不为空时，直接进行setImageBitmap
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    //参数1：启动任务输入的参数，参数2：后台任务执行的百分比，参数3，后台执行任务的返回方法
    private class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private String mUrl;

        public NewsAsyncTask(String stringUrl) {
            mUrl = stringUrl;
        }

        //doInBackground方法的参数是上面输入的第一个参数，返回的对象会传递给onPostExecute方法
        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap bitmap = getBitmapFormURL(url);
            if (bitmap != null){
                addBitmapToCache(url,bitmap); //将bitmap添加到缓存
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //根据url从listView中找到对应的ImageView
            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
            if (imageView != null && bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
            mAsyncTask.remove(this);
        }
    }
}
