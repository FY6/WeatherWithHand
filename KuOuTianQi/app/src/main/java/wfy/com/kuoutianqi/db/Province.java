package wfy.com.kuoutianqi.db;

import org.litepal.crud.DataSupport;

/**
 * Created by wfy on 2017/6/8.
 * <p>
 * 省份数据库
 */

public class Province extends DataSupport {
    private int provinceCode;
    private int id;
    private String name;

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

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    @Override
    public String toString() {
        return "Province{" +
                "provinceCode=" + provinceCode +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
