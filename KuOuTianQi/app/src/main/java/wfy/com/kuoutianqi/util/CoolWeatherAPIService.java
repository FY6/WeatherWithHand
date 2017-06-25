package wfy.com.kuoutianqi.util;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import wfy.com.kuoutianqi.db.City;
import wfy.com.kuoutianqi.db.County;
import wfy.com.kuoutianqi.db.Province;
import wfy.com.kuoutianqi.module.WeatherInfo;

/**
 * Created by wfy on 2017/6/8.
 * <p>
 * 定义访问服务器 API
 */

public interface CoolWeatherAPIService {
    @GET("api/china")
    Observable<List<Province>> queryProvinces();

    @GET("api/china/{provinceId}")
    Observable<List<City>> queryCitys(@Path("provinceId") int provinceId);

    @GET("api/china/{provinceId}/{cityId}")
    Observable<List<County>> queryCountys(@Path("provinceId") int provinceId, @Path("cityId") int cityId);

    @GET("api/weather")
    Observable<WeatherInfo> getWeatherInfo(@Query("cityid") String cityid, @Query("key") String key);

    //获取背景图的数据,返回背景图的url
    @GET("bing_pic")
    Observable<String> requestBingPicFromServer();
}
