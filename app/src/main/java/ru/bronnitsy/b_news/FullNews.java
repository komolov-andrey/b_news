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

public class FullNews extends Activity {

    public static String[] text = new String[MainActivity.n];
    public static String src_text = "";
    public static int position;

    private DatabaseHelper mDatabaseHelper = new DatabaseHelper(this, "news_db.db", null, 1);
    public SQLiteDatabase sdb;

    //MyLoading myLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.click_news);

        position = getIntent().getIntExtra("position", 0);

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

            //Нужно ссылаться на id

            String query = "SELECT * " + " FROM " + DatabaseHelper.DB_TABLE + " order by " + DatabaseHelper.COLUMN_ID + " DESC LIMIT " + MainActivity.n + "";
            Cursor cursor = sdb.rawQuery(query, null);

            int i = 0;
            while (i != (nom + 1)) {
                cursor.moveToNext();
                i++;
            }

            src_text = cursor.getString(cursor
                    .getColumnIndex(DatabaseHelper.COLUMN_SRCFULLNEWS));
            int src_id = cursor.getInt(cursor
                    .getColumnIndex(DatabaseHelper.COLUMN_ID));
            cursor.close();

            try {
                doc = Jsoup.connect(src_text).userAgent("Chrome").get();
                mBody = doc.select("div.news").first();

                Elements link = mBody.select("div.field.field-name-body.field-type-text-with-summary.field-label-hidden");
                article = link.text();

                //добавить в бд
                sdb = mDatabaseHelper.getWritableDatabase();
                String insertQuery = "UPDATE or IGNORE " + DatabaseHelper.DB_TABLE +
                        " set " + DatabaseHelper.COLUMN_TEXT + " = " + "'" + article + "'" + " WHERE " + DatabaseHelper.COLUMN_ID + " = " + src_id;
                sdb.execSQL(insertQuery);

            } catch (IOException e) {
                //чтение из бд
            }

            return article;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            TextView infoTextView = (TextView) findViewById(R.id.news_content);
            infoTextView.setText("     " + result);

        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        overridePendingTransition(R.anim.tap_in_left, R.anim.back_in_right);
    }
}