package ru.bronnitsy.b_news;

/**
 * Created by Андрюха on 19.05.2015.
 */

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
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
    public ProgressBar progressBar;

    private DatabaseHelper mDatabaseHelper = new DatabaseHelper(this, "news_db.db", null, 1);
    public SQLiteDatabase sdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.click_news);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.MULTIPLY);

        position = getIntent().getIntExtra("position", 0);

        GetFullNews gfn = new GetFullNews();
        gfn.execute(position);

    }

    class GetFullNews extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... numbers) {

            String article = "";
            int id = numbers[0];
            Document doc = null;
            Element mBody;

            sdb = mDatabaseHelper.getReadableDatabase();

            String query = "SELECT " + DatabaseHelper.COLUMN_SRCFULLNEWS + " FROM " + DatabaseHelper.DB_TABLE + " where " + DatabaseHelper.COLUMN_ID + " = " + id + "";
            Cursor cursor = sdb.rawQuery(query, null);
            cursor.moveToNext();

            src_text = cursor.getString(cursor
                    .getColumnIndex(DatabaseHelper.COLUMN_SRCFULLNEWS));
            cursor.close();

            try {
                doc = Jsoup.connect(src_text).userAgent("Chrome").get();
                mBody = doc.select("div.news").first();

                Elements link = mBody.select("div.field.field-name-body.field-type-text-with-summary.field-label-hidden");
                article = link.text();

                //добавить в бд
                sdb = mDatabaseHelper.getWritableDatabase();
                String insertQuery = "UPDATE " + DatabaseHelper.DB_TABLE +
                        " set " + DatabaseHelper.COLUMN_TEXT + " = " + "'" + article + "'" + " WHERE " + DatabaseHelper.COLUMN_ID + " = " + id;
                sdb.execSQL(insertQuery);

            } catch (IOException e) {
                //чтение из бд
                sdb = mDatabaseHelper.getReadableDatabase();

                query = "SELECT " + DatabaseHelper.COLUMN_TEXT + " FROM " + DatabaseHelper.DB_TABLE + " where " + DatabaseHelper.COLUMN_ID + " = " + id + "";
                cursor = sdb.rawQuery(query, null);
                cursor.moveToNext();

                article = cursor.getString(cursor
                        .getColumnIndex(DatabaseHelper.COLUMN_TEXT));
                cursor.close();
            }

            return article;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            TextView infoTextView = (TextView) findViewById(R.id.news_content);
            infoTextView.setText("     " + result);

            progressBar.setVisibility(ProgressBar.GONE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        overridePendingTransition(R.anim.tap_in_left, R.anim.back_in_right);
    }
}