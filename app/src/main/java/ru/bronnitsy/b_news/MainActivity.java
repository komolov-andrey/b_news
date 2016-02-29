package ru.bronnitsy.b_news;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    public static String[] images = new String[17];
    public static String[] headlines = new String[17];
    public static String[] date = new String[17];
    ListView listView = null;
    ProgressBar progressBar;
    private DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        mDatabaseHelper = new DatabaseHelper(this, "news_db.db", null, 1);
        SQLiteDatabase sdb;
        sdb = mDatabaseHelper.getReadableDatabase();

        if (!hasConnection(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();
            return;
        }

        listView = (ListView) findViewById(R.id.custom_list);

        parse p = new parse();
        p.execute();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView <?> a, View v, int position, long id) {
                ListItem newsData = (ListItem) listView.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, full_news.class);
                intent.putExtra("position", position);
                startActivity(intent);

                overridePendingTransition(R.anim.tap_in_right, R.anim.back_in_left);
            }
        });
    }

    private ArrayList<ListItem> getListData() {

        ArrayList<ListItem> listMockData = new ArrayList<ListItem>();

        for (int i = 0; i < headlines.length; i++) {
            ListItem newsData = new ListItem();
            newsData.setUrl(images[i]);
            newsData.setHeadline(headlines[i]);
            newsData.setDate(date[i]);

            listMockData.add(newsData);
        }
        return listMockData;
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
        if (id == R.id.about) {
            Intent intent = new Intent(MainActivity.this, about.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class parse extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... urls) {

            Document doc = null;
                try {
                    doc = Jsoup.connect("http://www.bronnitsy.ru/news").userAgent("Chrome").get();
                } catch (IOException e) {
                }
                Element mBody;

                //получение заголовка статьи

                for (int i = 0; i < 17; i++) {
                    try {
                        mBody = doc.select("div.news").get(i);
                        Elements links = mBody.select("a[href]");

                        headlines[i] = links.attr("title");

                    } catch (Exception e) {
                    }
                    //получение ссылки на фото статьи

                    try {
                        mBody = doc.select("div.news").get(i);
                        Elements links = mBody.select("[src]");

                        images[i] = links.attr("src");

                    } catch (Exception e) {
                    }


                    //Получение даты написания статьи

                    try {
                        mBody = doc.select("div.news").get(i);
                        String time = mBody.select("div.video_total").text();

                        date[i] = time;

                    } catch (Exception e) {
                    }
                }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ArrayList<ListItem> listData = getListData();
            ListView listView = (ListView) findViewById(R.id.custom_list) ;
            listView.setAdapter(new CustomListAdapter(getApplicationContext(), listData));

            LayoutAnimationController controller = AnimationUtils
                    .loadLayoutAnimation(getApplicationContext(), R.anim.list_layout_controller);
            listView.setLayoutAnimation(controller);

            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    public static boolean hasConnection(final Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        return false;
    }
}

