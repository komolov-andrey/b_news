package ru.bronnitsy.b_news;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Андрюха on 25.02.2016.
 */
public class MyLoading {

    Context context;
    ProgressDialog progress;

    public MyLoading(Context context) {
        this.context = context;

        progress = new ProgressDialog(context);
        progress.setMessage("Загрузка");
    }

    public void show(){
        progress.show();
    }

    public void dismiss(){
        progress.dismiss();
    }
}
