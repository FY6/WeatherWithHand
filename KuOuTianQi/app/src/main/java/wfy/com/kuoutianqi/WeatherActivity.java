package wfy.com.kuoutianqi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
import wfy.com.kuoutianqi.service.AutoUpdateService;
import wfy.com.kuoutianqi.util.CoolWeatherAPIService;
import wfy.com.kuoutianqi.util.Utility;
import wfy.com.kuoutianqi.view.AutoSwipeRefreshLayout;

import static wfy.com.kuoutianqi.R.id.weather_layout;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = WeatherActivity.class.getSimpleName();
    @BindView(weather_layout)
    ScrollView weatherLayout;
    @BindView(R.id.title_city)
    TextView titleCity;
    @BindView(R.id.title_update_time)
    TextView tittleUpdateTime;
    @BindView(R.id.degree_text)
    TextView degreeText;
    @BindView(R.id.weather_info_text)
    TextView weatherInfoText;
    @BindView(R.id.forecast_layout)
    LinearLayout forecastLayout;
    @BindView(R.id.aqi_text)
    TextView aqiText;
    @BindView(R.id.pm25_text)
    TextView pm25Text;
    @BindView(R.id.comfort_text)
    TextView comforText;
    @BindView(R.id.car_wash_text)
    TextView carWashText;
    @BindView(R.id.sport_text)
    TextView sportText;

    @BindView(R.id.bing_pic_img)
    ImageView bingPicImg;

    @BindView(R.id.swipe_refresh)
    public AutoSwipeRefreshLayout swipeRefreshLayout;

    private CompositeDisposable dis = new CompositeDisposable();
    private SharedPreferences prefs;

    private String mWeatheriId;

    @BindView(R.id.drawer_layout)
    public DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //大于21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //修改系统UI的显示，这里表示Activity的布局会显示在状态栏上面
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //设置状态栏的颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        //注入view事件
        ButterKnife.bind(this);
        //getDefaultSharedPreferences设个方法会自动以应用包名为前缀命名文件名
        prefs = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        presentWeather();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatheriId);
            }
        });
        swipeRefreshLayout.autoSwipeRefresh();
    }

    @OnClick(R.id.nav_button)
    public void switechCity() {
        drawerLayout.openDrawer(Gravity.START);
    }

    /**
     * 显示背景图片
     */
    private void showBackGroundImg() {
        String bing_pic = prefs.getString("bing_pic", null);

        if (!TextUtils.isEmpty(bing_pic)) {
            Glide.with(this)
                    .load(bing_pic)
                    .placeholder(R.mipmap.bg)
                    .error(R.mipmap.default_pic)
                    .crossFade(3000)
                    .into(bingPicImg);
        } else {
            loadBingPic("http://guolin.tech/api/");
        }
    }

    /**
     * 从服务器拉去图片url
     */
    private void loadBingPic(final String bingPicUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(bingPicUrl)
                .addConverterFactory(StringConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        Log.e("wea", retrofit.toString());
        Disposable subscribe = retrofit.create(CoolWeatherAPIService.class)
                .requestBingPicFromServer()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String picUrl) throws Exception {
                        prefs.edit().putString("bing_pic", picUrl).commit();
                        showBackGroundImg();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
        dis.add(subscribe);
    }

    private void presentWeather() {
        if (!Utility.isConnectNetWork(this)) {
            Toast.makeText(this, "当前网络不可用", Toast.LENGTH_SHORT).show();
        }
        showBackGroundImg();
        //有缓存是直接解析缓存
        if (!TextUtils.isEmpty(prefs.getString("weather", null))) {
            Disposable subscribe = Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                    e.onNext(prefs.getString("weather", null));
                }
            }).subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(@NonNull String cacheJsonData) throws Exception {
                            Gson gson = new Gson();
                            WeatherInfo weatherInfo = gson.fromJson(cacheJsonData, WeatherInfo.class);
                            mWeatheriId = weatherInfo.getHeWeather().get(0).getBasic().getId();
                            showWeatherInfo(weatherInfo);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            Log.e(TAG, "读取缓存失败");
                        }
                    });
            dis.add(subscribe);
        } else {//如果没有缓存，请求服务器
            mWeatheriId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatheriId);
        }
    }

    /**
     * 根据weatherId请求天气信息
     * <p>
     * 这里需要主要不要重新开子线程请求数据，因为已经是在子线程了，如果开了子线程，那么可能会有意想不到的
     * 的错误出现
     *
     * @param weather_id
     */
    public void requestWeather(final String weather_id) {
        mWeatheriId = weather_id;
        loadBingPic("http://guolin.tech/api/");
        Disposable subscribe = Utility.getCoolWeatherAPIService()
                .getWeatherInfo(weather_id, "f87fe7e9d8a14d2d94bf1cb2bd91161b")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Consumer<WeatherInfo>() {
                    @Override
                    public void accept(@NonNull WeatherInfo weatherInfo) throws Exception {
                        Gson gson = new Gson();
                        prefs.edit().putString("weather", gson.toJson(weatherInfo).toString()).commit();
                        showWeatherInfo(weatherInfo);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Toast.makeText(WeatherActivity.this, "请求天气信息失败", Toast.LENGTH_SHORT).show();
                        throwable.printStackTrace();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
        dis.add(subscribe);
    }


    /**
     * 展示weatherinfo 信息
     */
    private void showWeatherInfo(WeatherInfo weatherInfo) {
        Log.e(TAG, "show:" + weatherInfo.toString());
        swipeRefreshLayout.setRefreshing(false);


        WeatherInfo.HeWeatherBean heWeatherBean = weatherInfo.getHeWeather().get(0);
        WeatherInfo.HeWeatherBean.BasicBean basic = heWeatherBean.getBasic();
        String cityName = basic.getCity();
        String updateTime = basic.getUpdate().getLoc().split(" ")[1];

        WeatherInfo.HeWeatherBean.NowBean now = heWeatherBean.getNow();
        String degree = now.getTmp() + "'C";
        String info = now.getCond().getTxt();

        titleCity.setText(cityName);
        tittleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(info);
        forecastLayout.removeAllViews();

        List<WeatherInfo.HeWeatherBean.DailyForecastBean> daily_forecast = heWeatherBean.getDaily_forecast();
        for (WeatherInfo.HeWeatherBean.DailyForecastBean bean : daily_forecast) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);

            TextView dataText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);

            dataText.setText(bean.getDate());
            infoText.setText(bean.getCond().getTxt_d());
            maxText.setText(bean.getTmp().getMax());
            minText.setText(bean.getTmp().getMin());

            forecastLayout.addView(view);

        }

        if (heWeatherBean.getAqi() != null) {
            aqiText.setText(heWeatherBean.getAqi().getCity().getAqi());
            pm25Text.setText(heWeatherBean.getAqi().getCity().getPm25());
        }

        String comfort = "舒适度: " + heWeatherBean.getSuggestion().getComf().getTxt();
        String carWash = "洗车指数: " + heWeatherBean.getSuggestion().getCw().getTxt();
        String sport = "运动指数: " + heWeatherBean.getSuggestion().getSport().getTxt();

        comforText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);


        if (weatherInfo != null && "ok".equals(weatherInfo.getHeWeather().get(0).getStatus())) {
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        } else {
            Toast.makeText(this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dis.clear();
    }
}
