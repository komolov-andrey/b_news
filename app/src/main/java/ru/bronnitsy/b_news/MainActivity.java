package ru.bronnitsy.b_news;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        parse p = new parse();
        p.execute();

        final ListView listView = (ListView) findViewById(R.id.custom_list);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView <?> a, View v, int position, long id) {
                ListItem newsData = (ListItem) listView.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, full_news.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }

    private ArrayList<ListItem> getListData() {

        ArrayList<ListItem> listMockData = new ArrayList<ListItem>();

        for (int i = 0; i < images.length; i++) {
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
                    doc = Jsoup.connect("http://www.bronnitsy.ru/news").get();
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
            final ListView listView = (ListView) findViewById(R.id.custom_list);
            listView.setAdapter(new CustomListAdapter(getApplicationContext(), listData));
        }
    }


}

