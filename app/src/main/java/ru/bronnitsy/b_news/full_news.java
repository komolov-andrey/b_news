package ru.bronnitsy.b_news;

/**
 * Created by Андрюха on 19.05.2015.
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class full_news extends Activity {

    public static String[] text = new String[17];
    public static int position;

    MyLoading myLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.click_news);

        myLoading = new MyLoading(this);
        myLoading.show();
        //получили ссылки на описание новости
        get_short_news gsn = new get_short_news();
        gsn.execute();

        position = getIntent().getIntExtra("position", 0);// default пусть 0
        //полное оприсание новости
        get_full_news gfn = new get_full_news();
        gfn.execute(position);

    }
    class get_short_news extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... urls) {

            Document doc = null;
            try {
                doc = Jsoup.connect("http://www.bronnitsy.ru/news").userAgent("Chrome").get();
            } catch (IOException e) {
            }
            Element mBody;

            //Получение ссылки на статью
            for (int i=0;i<17;i++) {
                try {
                    mBody = doc.select("div.news").get(i);
                    Elements links = mBody.select("a[href]");

                    text[i] = "http://www.bronnitsy.ru" + links.attr("href");

                } catch (Exception e) {
                }

            }
            return text;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            for (int i=0;i<17;i++)
                text[i] = result[i];
        }
    }

    class get_full_news extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... numbers) {

                String article = "";
                int nom = numbers[0];
                Document doc = null;
                Element mBody;
            try {
                    doc = Jsoup.connect(text[nom]).userAgent("Chrome").get();
                    mBody = doc.select("div.news").first();

                    Elements link = mBody.select("div.field.field-name-body.field-type-text-with-summary.field-label-hidden");
                    article = link.text();

            } catch (IOException e) {
            }

            return article;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            TextView infoTextView = (TextView)findViewById(R.id.news_content);
            infoTextView.setText("     " + result);

            myLoading.dismiss();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.tap_in_left, R.anim.back_in_right);
    }
}