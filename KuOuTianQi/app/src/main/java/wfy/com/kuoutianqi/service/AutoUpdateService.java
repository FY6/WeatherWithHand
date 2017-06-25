package wfy.com.kuoutianqi.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import wfy.com.kuoutianqi.factory.StringConverterFactory;
import wfy.com.kuoutianqi.module.WeatherInfo;
import wfy.com.kuoutianqi.util.CoolWeatherAPIService;
import wfy.com.kuoutianqi.util.Utility;

import static android.content.ContentValues.TAG;

public class AutoUpdateService extends Service {

    private SharedPreferences mPref;
    private String mWeatheriId;

    CompositeDisposable dis = new CompositeDisposable();

    @Override
    public void onCreate() {
        super.onCreate();
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        dis.clear();
        updateWeather();
        updateBingPic();
        Log.e("GGG", "satrt service");
        long initialDelay = 8 * 60 * 60 * 1000;
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long l = SystemClock.elapsedRealtime() + initialDelay;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, l, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        Log.e("GGG", "satrt updateWeather");
        //有缓存是直接解析缓存
        if (!TextUtils.isEmpty(mPref.getString("weather", null))) {
            Disposable subscribe = Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                    e.onNext(mPref.getString("weather", null));
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(@NonNull String cacheJsonData) throws Exception {
                            Gson gson = new Gson();
                            WeatherInfo weatherInfo = gson.fromJson(cacheJsonData, WeatherInfo.class);
                            mWeatheriId = weatherInfo.getHeWeather().get(0).getBasic().getId();
                            requestWeather(mWeatheriId);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            Log.e(TAG, "更新天气缓存失败");
                        }
                    });
            dis.add(subscribe);
        }
    }

    public void requestWeather(final String weather_id) {
        mWeatheriId = weather_id;
        Disposable subscribe = Utility.getCoolWeatherAPIService()
                .getWeatherInfo(weather_id, "f87fe7e9d8a14d2d94bf1cb2bd91161b")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherInfo>() {
                    @Override
                    public void accept(@NonNull WeatherInfo weatherInfo) throws Exception {
                        Gson gson = new Gson();
                        Log.e("update", "update success");
                        mPref.edit().putString("weather", gson.toJson(weatherInfo).toString()).commit();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.e(TAG, "更请求天气缓存失败");
                        throwable.printStackTrace();
                    }
                });
        dis.add(subscribe);
    }

    /**
     * 从服务器拉去图片url
     */
    private void updateBingPic() {
        Log.e("GGG", "satrt updateBingPic");
        String url = "http://guolin.tech/api/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(StringConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        Log.e("pic", retrofit.toString());
        Disposable subscribe = retrofit.create(CoolWeatherAPIService.class)
                .requestBingPicFromServer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String picUrl) throws Exception {
                        mPref.edit().putString("bing_pic", picUrl).commit();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
        dis.add(subscribe);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dis.clear();
    }
}


