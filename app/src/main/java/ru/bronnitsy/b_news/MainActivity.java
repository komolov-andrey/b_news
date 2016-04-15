package ru.bronnitsy.b_news;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    public static final int n = 17;
    public String[] images = new String[n];
    public String[] headlines = new String[n];
    public String[] date = new String[n];
    public String[] src_full_news = new String[n];
    ListView listView;
    ProgressBar progressBar;
    Button up;
    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeColors(
                Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);

        up = (Button) findViewById(R.id.button);
        up.setVisibility(View.INVISIBLE);

        mDatabaseHelper = new DatabaseHelper(this, "news_db.db", null, 1);

        LoadingData loadingData = new LoadingData();
        loadingData.execute();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                LoadingData loadingData = new LoadingData();
                loadingData.execute();
            }
        });
    }

    CustomListAdapter adapter;
    private void setListView() {

        listView = (ListView) findViewById(R.id.custom_list);
        ArrayList<ListItem> listData = getListData();
        listView.setAdapter(adapter = new CustomListAdapter(getApplicationContext(), listData));

        LayoutAnimationController controller = AnimationUtils
                .loadLayoutAnimation(getApplicationContext(), R.anim.list_layout_controller);
        listView.setLayoutAnimation(controller);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {

                try {
                    sdb = mDatabaseHelper.getReadableDatabase();

                    String query = "SELECT * FROM " + DatabaseHelper.DB_TABLE + " order by " + DatabaseHelper.COLUMN_ID + " desc";
                    Cursor cursor = sdb.rawQuery(query, null);
                    cursor.moveToPosition(position);

                    int _id = cursor.getInt(cursor
                            .getColumnIndex(DatabaseHelper.COLUMN_ID));
                    cursor.close();

                    Intent intent = new Intent(MainActivity.this, FullNews.class);
                    intent.putExtra("position", _id);
                    startActivity(intent);
                } catch (Exception e) {

                    Toast.makeText(getApplicationContext(),
                            "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                }
                overridePendingTransition(R.anim.tap_in_right, R.anim.back_in_left);
            }
        });
        //flag
        final int[] last_item = {1};

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    mSwipeRefreshLayout.setEnabled(true);
                    up.setVisibility(View.GONE);
                }
                else {
                    mSwipeRefreshLayout.setEnabled(false);
                    up.setVisibility(View.VISIBLE);
                }

                boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

                if (last_item[0] != totalItemCount && loadMore) {

                        AddingData addingData = new AddingData();
                        addingData.execute(totalItemCount);
                        last_item[0] = firstVisibleItem + visibleItemCount;
                }
            }
        });

        progressBar.setVisibility(ProgressBar.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
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

    public void Click_up(View view)
    {
        listView.smoothScrollToPosition(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    class LoadingData extends AsyncTask<Void, Void, List<String[]>> {

        private boolean isInternetConnection = true;

        @Override
        protected List<String[]> doInBackground(Void... urls) {

            try {

                isInternetConnection = false;

                Document doc = null;
                doc = Jsoup.connect("http://www.bronnitsy.ru/news").userAgent("Chrome").timeout(7000).get();

                Element mBody;

                //получение заголовка статьи

                for (int i = n - 1; i >= 0; i--) {

                    mBody = doc.select("div.news").get(i);
                    Elements links = mBody.select("a[href]");

                    headlines[i] = links.attr("title");

                    //получение ссылки на фото статьи

                    mBody = doc.select("div.news").get(i);
                    links = mBody.select("[src]");

                    images[i] = links.attr("src");

                    //Получение даты написания статьи

                    mBody = doc.select("div.news").get(i);
                    String time = mBody.select("div.video_total").text();

                    date[i] = time;

                    //Получение ссылки на полное описание новости

                    mBody = doc.select("div.news").get(i);
                    links = mBody.select("a[href]");

                    src_full_news[i] = "http://www.bronnitsy.ru" + links.attr("href");


                    SQLiteDatabase sqdb = mDatabaseHelper.getWritableDatabase();
                    String insertQuery = "INSERT or IGNORE INTO " +
                            DatabaseHelper.DB_TABLE +
                            " (" + DatabaseHelper.COLUMN_TITLE + ", " + DatabaseHelper.COLUMN_DATE + ", " + DatabaseHelper.COLUMN_IMAGE + ", " + DatabaseHelper.COLUMN_SRCFULLNEWS + ") VALUES (" +
                            "'" + headlines[i] + "'" + ", " + "'" + date[i] + "'" + ", " + "'" + images[i] + "'" + ", " + "'" + src_full_news[i] + "'" + ")";
                    sqdb.execSQL(insertQuery);
                }
                isInternetConnection = true;

            } catch (Exception e) {

                try {
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
                        src_full_news[i] = cursor.getString(cursor
                                .getColumnIndex(DatabaseHelper.COLUMN_SRCFULLNEWS));
                        i++;
                    }
                    cursor.close();
                } catch (Exception e1) {

                }
            }

            List<String[]> list = new ArrayList<>();

            list.add(headlines);
            list.add(date);
            list.add(images);
            list.add(src_full_news);

            return list;
        }

        @Override
        protected void onPostExecute(List<String[]> list) {
            super.onPostExecute(list);

            if (!isInternetConnection) {
                Toast.makeText(getApplicationContext(),
                        "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();
            }

            headlines = list.get(0);
            date = list.get(1);
            images = list.get(2);
            src_full_news = list.get(3);

            setListView();
        }
    }

    class AddingData extends AsyncTask<Integer, Void, ArrayList<ListItem>> {

        @Override
        protected ArrayList<ListItem> doInBackground(Integer... count) {

            ArrayList<ListItem> list = new ArrayList<>();

            try {
                sdb = mDatabaseHelper.getReadableDatabase();

                String query = "SELECT * FROM " + DatabaseHelper.DB_TABLE + " ORDER BY " + DatabaseHelper.COLUMN_ID + " DESC";
                Cursor cursor = sdb.rawQuery(query, null);

                if (cursor.getCount() >= count[0]) {
                    cursor.moveToPosition(count[0] - 1);

                    int i = 1;

                    while ((cursor.moveToNext()) || (i <= n)) {
                        ListItem newsData = new ListItem();

                        images[i] = cursor.getString(cursor
                                .getColumnIndex(DatabaseHelper.COLUMN_IMAGE));
                        newsData.setUrl(images[i]);
                        headlines[i] = cursor.getString(cursor
                                .getColumnIndex(DatabaseHelper.COLUMN_TITLE));
                        newsData.setHeadline(headlines[i]);
                        date[i] = cursor.getString(cursor
                                .getColumnIndex(DatabaseHelper.COLUMN_DATE));
                        newsData.setDate(date[i]);

                        list.add(newsData);
                        i++;
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                //обработать если нет в БД или не надо
            }

            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<ListItem> results) {
            super.onPostExecute(results);

            adapter.add(results);
            adapter.notifyDataSetChanged();
//            int index = listView.getFirstVisiblePosition();
//            int top = (listView.getChildAt(0) == null) ? 0 : listView.getChildAt(0).getTop();
//            listView.setSelectionFromTop(index, top);
        }
    }
}