package ru.bronnitsy.b_news;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity {

    public static final int n = 17;
    public String[] images = new String[n];
    public String[] headlines = new String[n];
    public String[] date = new String[n];
    ListView listView;
    ProgressBar progressBar;
    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;
    public static Boolean checkInt = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        mDatabaseHelper = new DatabaseHelper(this, "news_db.db", null, 1);

        CheckInternet checkInternet = new CheckInternet();
        checkInternet.execute();

        try {
            checkInt = checkInternet.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (!checkInt) {
            Toast.makeText(getApplicationContext(),
                    "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();

            LoadDataFromDB loadDataFromDB = new LoadDataFromDB();
            loadDataFromDB.execute();
        } else {
            parse p = new parse();
            p.execute();
        }

        listView = (ListView) findViewById(R.id.custom_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                //ListItem newsData = (ListItem) listView.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, FullNews.class);
                intent.putExtra("position", position);
                startActivity(intent);

                overridePendingTransition(R.anim.tap_in_right, R.anim.back_in_left);
            }
        });
    }


    private void setListView() {

        ArrayList<ListItem> listData = getListData();
        listView.setAdapter(new CustomListAdapter(getApplicationContext(), listData));

        LayoutAnimationController controller = AnimationUtils
                .loadLayoutAnimation(getApplicationContext(), R.anim.list_layout_controller);
        listView.setLayoutAnimation(controller);

        progressBar.setVisibility(ProgressBar.GONE);
    }

    private ArrayList<ListItem> getListData() {

        ArrayList<ListItem> listMockData = new ArrayList<>();

        for (int i = 0; i < n; i++) {
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

        if (id == R.id.about) {
            Intent intent = new Intent(MainActivity.this, about.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class parse extends AsyncTask<Void, Void, List<String[]>> {

        //String[] images = new String[n];
        //String[] headlines = new String[n];
        //String[] date = new String[n];
        String[] src_full_news = new String[n];

        @Override
        protected List<String[]> doInBackground(Void... urls) {

            Document doc = null;
            try {
                doc = Jsoup.connect("http://www.bronnitsy.ru/news").userAgent("Chrome").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Element mBody;

            //получение заголовка статьи

            for (int i = n - 1; i >= 0; i--) {
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

                try {
                    mBody = doc.select("div.news").get(i);
                    Elements links = mBody.select("a[href]");

                    src_full_news[i] = "http://www.bronnitsy.ru" + links.attr("href");

                } catch (Exception e) {
                }

                SQLiteDatabase sqdb = mDatabaseHelper.getWritableDatabase();
                String insertQuery = "INSERT or IGNORE INTO " +
                        DatabaseHelper.DB_TABLE +
                        " (" + DatabaseHelper.COLUMN_TITLE + ", " + DatabaseHelper.COLUMN_DATE + ", " + DatabaseHelper.COLUMN_IMAGE + ", " + DatabaseHelper.COLUMN_SRCFULLNEWS + ") VALUES (" +
                        "'" + headlines[i] + "'" + ", " + "'" + date[i] + "'" + ", " + "'" + images[i] + "'" + ", " + "'" + src_full_news[i] + "'" + ")";
                sqdb.execSQL(insertQuery);
            }

            List<String[]> a = new ArrayList<>();

            a.add(headlines);
            a.add(date);
            a.add(images);

            return a;
        }

        @Override
        protected void onPostExecute(List<String[]> spisok) {
            super.onPostExecute(spisok);

            headlines = spisok.get(0);
            date = spisok.get(1);
            images = spisok.get(2);
            setListView();
        }
    }

    class LoadDataFromDB extends AsyncTask<Void, Void, List<String[]>> {

        //String[] images = new String[n];
        //String[] headlines = new String[n];
        //String[] date = new String[n];

        @Override
        protected List<String[]> doInBackground(Void... urls) {

            sdb = mDatabaseHelper.getReadableDatabase();

            String query = "SELECT * FROM " + DatabaseHelper.DB_TABLE + " ORDER BY " + DatabaseHelper.COLUMN_ID + " DESC LIMIT " + n + "";
            Cursor cursor = sdb.rawQuery(query, null);

            int i = 0;
            while (cursor.moveToNext()) {

                images[i] = cursor.getString(cursor
                        .getColumnIndex(DatabaseHelper.COLUMN_IMAGE));
                headlines[i] = cursor.getString(cursor
                        .getColumnIndex(DatabaseHelper.COLUMN_TITLE));
                date[i] = cursor.getString(cursor
                        .getColumnIndex(DatabaseHelper.COLUMN_DATE));
                i++;
            }
            cursor.close();


            List<String[]> a = new ArrayList<>();

            a.add(headlines);
            a.add(date);
            a.add(images);

            return a;
        }

        @Override
        protected void onPostExecute(List<String[]> spisok) {
            super.onPostExecute(spisok);
            headlines = spisok.get(0);
            date = spisok.get(1);
            images = spisok.get(2);
            setListView();
        }
    }

    class CheckInternet extends AsyncTask<Void, Void, Boolean> {

        public boolean isInternetConnection = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            MyLoading mm = new MyLoading();
//            mm.show();
        }

        @Override
        protected Boolean doInBackground(Void... agrs) {

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            // проверка подключения
            if (activeNetwork != null && activeNetwork.isConnected()) {
                try {
                    // тест доступности внешнего ресурса
                    URL url = new URL("http://www.bronnitsy.ru/news");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setRequestProperty("User-Agent", "test");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(3000); // Timeout в секундах
                    urlc.connect();
                    // статус ресурса OK
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                    // иначе проверка провалилась
                    return false;

                } catch (IOException e) {
                    return false;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }
}