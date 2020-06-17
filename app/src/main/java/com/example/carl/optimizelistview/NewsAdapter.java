package com.example.carl.optimizelistview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    private List<NewsBean> newsBeanList = new ArrayList<>();
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private int mStart, mEnd;
    public static String[] URLS;
    private boolean mFirstIn;

    public NewsAdapter(Context context, List<NewsBean> data, ListView listView) {
        newsBeanList = data;
        mInflater = LayoutInflater.from(context);
        mImageLoader = new ImageLoader(listView);

        //将图片的url存储在数组中
        URLS = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            URLS[i] = data.get(i).newsIconUrl;
        }

        listView.setOnScrollListener(this);
        mFirstIn = true;
    }

    @Override
    public int getCount() {
        return newsBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return newsBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //判断是否有缓存
        if (convertView == null) {
            //通过LayoutInflate实例化布局
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_layout, parent, false);
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(viewHolder);
        } else {
            //通过tag找到缓存的布局
            viewHolder = (ViewHolder) convertView.getTag();
        }
        NewsBean newsBean = newsBeanList.get(position);

        String urlString = newsBean.newsIconUrl;
        viewHolder.ivIcon.setTag(urlString); // 将ImageView与url绑定
        //普通异步加载
        // mImageLoader.showImageByThread(viewHolder.ivIcon,urlString);
        mImageLoader.showImageByAsyncTask(viewHolder.ivIcon,urlString);
        viewHolder.tvTitle.setText(newsBean.newsTitle);
        viewHolder.tvContent.setText(newsBean.newsContent);
        return convertView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState){
            case SCROLL_STATE_IDLE:  //滑动停止时。
                mImageLoader.loadImages(mStart, mEnd);
                break;
            case SCROLL_STATE_TOUCH_SCROLL: //正在滑动时
                mImageLoader.cancelAllTasks();
                break;
            case SCROLL_STATE_FLING: //手指抛动时，即手指用力滑动在离开后ListView由于惯性而继续滑动

                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart = firstVisibleItem;
        mEnd = firstVisibleItem + visibleItemCount;
        //第一次的时候预加载
        if (mFirstIn && visibleItemCount > 0){
            mImageLoader.loadImages(mStart, mEnd);
            mFirstIn = false;
        }

    }

    private void log(String str){
        Log.i("chen",str);
    }
    //使用ViewHolder
    private static class ViewHolder {
        private TextView tvTitle, tvContent;
        private ImageView ivIcon;
    }

}
