package wfy.com.kuoutianqi.app;

import android.app.Application;

import org.litepal.LitePal;

public class KuOuWeather extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
