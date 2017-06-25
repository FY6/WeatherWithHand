package wfy.com.kuoutianqi.db;

import org.litepal.crud.DataSupport;

/**
 * Created by wfy on 2017/6/8.
 * 市县数据库
 */

public class County extends DataSupport {
    private int cityId;
    private int id;
    private String name;
    private String weather_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeather_id() {
        return weather_id;
    }

    public void setWeather_id(String weather_id) {
        this.weather_id = weather_id;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    @Override
    public String toString() {
        return "County{" +
                "cityId=" + cityId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", weather_id='" + weather_id + '\'' +
                '}';
    }
}
