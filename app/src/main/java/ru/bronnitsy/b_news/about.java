package ru.bronnitsy.b_news;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Андрюха on 19.05.2015.
 */
public class about extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);
        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        imgView.setImageResource(R.drawable.logo);

        TextView txtView = (TextView) findViewById(R.id.textView);
        txtView.setText("Это неофициальный клиент\nВся информация взята с сайта \nhttp://www.bronnitsy.ru\n\nРазработчик Андрей Комолов");

    }
}