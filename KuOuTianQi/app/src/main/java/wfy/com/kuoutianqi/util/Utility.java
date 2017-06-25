package wfy.com.kuoutianqi.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by wfy on 2017/6/8.
 */

public class Utility {
    private static CoolWeatherAPIService mCoolWeatherAPIService;

    private Utility() {
    }

    public static CoolWeatherAPIService getCoolWeatherAPIService() {
        if (null == mCoolWeatherAPIService) {
            synchronized (Utility.class) {
                if (null == mCoolWeatherAPIService) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(Global.URL)
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    mCoolWeatherAPIService = retrofit.create(CoolWeatherAPIService.class);
                }
            }
        }
        return mCoolWeatherAPIService;
    }

    /**
     * 判断当前网络状态是否为已连接状态
     *
     * @param context
     * @return
     */
    public static boolean isConnectNetWork(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

        //判断当前网络是否可用
        NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null) {
            return activeNetworkInfo.isAvailable();
        }
        return false;
    }
}
