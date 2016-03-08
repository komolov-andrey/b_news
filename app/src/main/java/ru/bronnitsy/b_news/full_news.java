package ru.bronnitsy.b_news;

/**
 * Created by Андрюха on 19.05.2015.
 */

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class full_news extends Activity {

    public static String[] text = new String[MainActivity.n];
    public static String src_text = "";
    public static int position;

    private DatabaseHelper mDatabaseHelper = new DatabaseHelper(this, "news_db.db", null, 1);;
    public SQLiteDatabase sdb;

    MyLoading myLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.click_news);

        myLoading = new MyLoading(this);
        myLoading.show();

        position = getIntent().getIntExtra("position", 0);// default пусть 0
        get_full_news gfn = new get_full_news();
        gfn.execute(position);

    }

    class get_full_news extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... numbers) {

                String article = "";
                int nom = numbers[0];
                Document doc = null;
                Element mBody;

            sdb = mDatabaseHelper.getReadableDatabase();

            String query = "SELECT * " + " FROM " + DatabaseHelper.DB_TABLE + " order by " + DatabaseHelper.COLUMN_ID + " DESC LIMIT " + MainActivity.n + "";
            Cursor cursor = sdb.rawQuery(query, null);

            int i = 0;
            while (i!=(nom+1)) {
            cursor.moveToNext();
                i++;
            }
            //добавить в бд
            src_text = cursor.getString(cursor
                    .getColumnIndex(DatabaseHelper.COLUMN_SRCFULLNEWS));
            cursor.close();

            try {
                    doc = Jsoup.connect(src_text).userAgent("Chrome").get();
                    mBody = doc.select("div.news").first();

                    Elements link = mBody.select("div.field.field-name-body.field-type-text-with-summary.field-label-hidden");
                    article = link.text();

            } catch (IOException e) {
                //чтение из бд
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