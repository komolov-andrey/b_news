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
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity {

    public final int n = 17;
    public String[] images = new String[n];
    public String[] headlines = new String[n];
    public String[] date = new String[n];
    ListView listView;
    ProgressBar progressBar;
    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        mDatabaseHelper = new DatabaseHelper(this, "news_db.db", null, 1);

        if (!hasConnection(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();
            return;
        }

        parse p = new parse();
        p.execute();

        try {
            List<String[]> spisok = p.get();

            headlines = spisok.get(0);
            date = spisok.get(1);
            images = spisok.get(2);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        ArrayList<ListItem> listData = getListData();
        listView = (ListView) findViewById(R.id.custom_list) ;
        listView.setAdapter(new CustomListAdapter(getApplicationContext(), listData));

        LayoutAnimationController controller = AnimationUtils
                .loadLayoutAnimation(getApplicationContext(), R.anim.list_layout_controller);
        listView.setLayoutAnimation(controller);

        progressBar.setVisibility(ProgressBar.INVISIBLE);

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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about) {
            Intent intent = new Intent(MainActivity.this, about.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class parse extends AsyncTask<Void, Void, List<String[]>> {

        String[] images = new String[n];
        String[] headlines = new String[n];
        String[] date = new String[n];
        @Override
        protected List<String[]> doInBackground(Void... urls) {

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

                    SQLiteDatabase sqdb = mDatabaseHelper.getWritableDatabase();
                    String insertQuery = "INSERT or IGNORE INTO " +
                            DatabaseHelper.DB_TABLE +
                            " (" + DatabaseHelper.COLUMN_TITLE + ", " + DatabaseHelper.COLUMN_DATE + ", " + DatabaseHelper.COLUMN_IMAGE + ") VALUES (" +
                            "'" + headlines[i] + "'" + ", " + "'" + date[i] + "'" + ", " + "'" + images[i] + "'" + ")";
                    sqdb.execSQL(insertQuery);
                }

            List<String[]> a = new ArrayList<String[]>();

            a.add(headlines);
            a.add(date);
            a.add(images);

            return a;
        }

        @Override
        protected void onPostExecute(List<String[]> str) {
            super.onPostExecute(str);
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

