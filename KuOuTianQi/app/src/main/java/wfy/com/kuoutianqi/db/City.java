package wfy.com.kuoutianqi.db;

import org.litepal.crud.DataSupport;

/**
 * Created by wfy on 2017/6/8.
 * <p>
 * 城市数据库
 */

public class City extends DataSupport {
    private int cityCode;
    private int provinceId;

    private int id;
    private String name;


    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

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

    @Override
    public String toString() {
        return "City{" +
                "cityCode=" + cityCode +
                ", provinceId=" + provinceId +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
