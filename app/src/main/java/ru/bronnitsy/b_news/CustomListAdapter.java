package ru.bronnitsy.b_news;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Андрюха on 18.05.2015.
 */
public class CustomListAdapter extends BaseAdapter {
    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public CustomListAdapter(Context context, ArrayList<ListItem> listData) {
        this.listData = listData;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            holder = new ViewHolder();
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.headlineView = (TextView) convertView.findViewById(R.id.title);
        holder.reportedDateView = (TextView) convertView.findViewById(R.id.date);
        holder.imageView = (ImageView) convertView.findViewById(R.id.thumbImage);

        ListItem newsItem = (ListItem) getItem(position);

        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new CustomInterceptor());
        Picasso picasso = new Picasso.Builder(context)
                .downloader(new OkHttpDownloader(client))
                .build();

        picasso.load(newsItem.getUrl().substring(0, newsItem.getUrl().lastIndexOf("?")))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imageView);

        holder.headlineView.setText(newsItem.getHeadline());
        holder.reportedDateView.setText(newsItem.getDate());
        return convertView;
    }

    static class ViewHolder {
        TextView headlineView;
        TextView reportedDateView;
        ImageView imageView;
    }

    public class CustomInterceptor implements Interceptor {

        public CustomInterceptor() {}

        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .removeHeader("User-Agent")
                    .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                    .build();

            return chain.proceed(requestWithUserAgent);
        }

    }
}