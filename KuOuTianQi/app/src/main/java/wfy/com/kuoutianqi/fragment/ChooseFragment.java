package wfy.com.kuoutianqi.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import wfy.com.kuoutianqi.MainActivity;
import wfy.com.kuoutianqi.R;
import wfy.com.kuoutianqi.WeatherActivity;
import wfy.com.kuoutianqi.db.City;
import wfy.com.kuoutianqi.db.County;
import wfy.com.kuoutianqi.db.Province;
import wfy.com.kuoutianqi.util.Utility;

public class ChooseFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;


    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.list_view)
    ListView listView;

    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selecttedProvince;
    private City selectedCity;
    private int currentLevel;
    private ArrayAdapter<String> adapter;
    private ProgressDialog progressDialog;

    private CompositeDisposable dis = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        ButterKnife.bind(this, view);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selecttedProvince = provinceList.get(position);
                    //拿城市数据
                    queryCities(selecttedProvince.getProvinceCode());
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    //拿区、县数据
                    queryCounties(selectedCity.getProvinceId(), selectedCity.getCityCode());
                } else if (currentLevel == LEVEL_COUNTY) {
                    County county = countyList.get(position);
                    String weather_id = county.getWeather_id();

                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weather_id);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                        weatherActivity.swipeRefreshLayout.setRefreshing(true);
                        weatherActivity.drawerLayout.closeDrawers();
                        weatherActivity.requestWeather(weather_id);
                    }
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    Log.e("TAG", "click selectedCity: " + selectedCity.getCityCode() + "");
                    //拿数据
                    queryCities(selectedCity.getProvinceId());
                } else if (currentLevel == LEVEL_CITY) {
                    //拿数据
                    queryProvinces();
                }
            }
        });
        ////拿省份数据
        queryProvinces();
    }

    /**
     * 查询全国所有省份，有限从数据库中查询，如果没有在去服务器拉去数据并保存至数据库
     * <p>
     * <p>
     * <p>
     * 这里需要注意的是：因为我们使用的LitePal操作数据库，并且使用的bean和数据库模型一样，，而且我们还是从数据库取出数据
     * 展示的，所以我们不能使用id作为bean的id，，，其中的id是数据库中的id，不一样的，座椅我们需要用一个code表示，服务器返回给我们的id
     */
    private void queryProvinces() {
        tvTitle.setText("中国");
        btnBack.setVisibility(View.GONE);

        provinceList = DataSupport.findAll(Province.class);

        if (provinceList.size() > 0) {
            Log.e("TAG", "database");
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {//否则从服务器拉去数据
            queryFromServer("province");
        }
    }

    private void queryCities(int provinceId) {
        tvTitle.setText(selecttedProvince.getName());
        btnBack.setVisibility(View.VISIBLE);

        cityList =
                DataSupport.where("provinceId = ?", String.valueOf(provinceId)).find(City.class);

        if (cityList.size() > 0) {
            dataList.clear();
            for (City c : cityList) {
                dataList.add(c.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer("city", provinceId);
        }
    }

    private void queryCounties(int provinceId, int cityid) {
        tvTitle.setText(selectedCity.getName());
        btnBack.setVisibility(View.VISIBLE);

        countyList = DataSupport.where("cityId = ?", String.valueOf(cityid)).find(County.class);

        if (countyList.size() > 0) {
            dataList.clear();
            for (County c : countyList) {
                dataList.add(c.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer("county", provinceId, cityid);
        }
    }

    private void queryFromServer(String type, final int... agrs) {
        showProgressDialog();
        switch (type) {
            case "province":
                Disposable subscribe = Utility.getCoolWeatherAPIService()
                        .queryProvinces()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnNext(new Consumer<List<Province>>() {
                            @Override
                            public void accept(@NonNull List<Province> provinces) throws Exception {
                                Log.e("TAG", provinces.size() + "--");

                                ArrayList<Province> provinces1 = new ArrayList<>();
                                for (Province p : provinces) {
                                    Log.e("pro", p.getName());
                                    p.setProvinceCode(p.getId());//这里的id材质真正的id
                                    provinces1.add(p);
                                }
                                DataSupport.saveAll(provinces1);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<Province>>() {
                            @Override
                            public void accept(@NonNull List<Province> provinces) throws Exception {
                                Log.e("pro", provinces.toString());
                                queryProvinces();
                                closeProgressDialog();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                closeProgressDialog();
//                                Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                                Snackbar.make(ChooseFragment.this.getView(), "请求数据失败", 5000)
                                        .setAction("重新加载", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                queryProvinces();
                                            }
                                        }).show();
                            }
                        });
                dis.add(subscribe);
                break;
            case "city":
                Disposable subscribe1 = Utility.getCoolWeatherAPIService()
                        .queryCitys(agrs[0])
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnNext(new Consumer<List<City>>() {
                            @Override
                            public void accept(@NonNull List<City> cities) throws Exception {
                                ArrayList<City> cities1 = new ArrayList<>();
                                for (City c : cities) {
                                    Log.e("city", c.getName());
                                    c.setCityCode(c.getId());//这里的id材质真正的id
                                    c.setProvinceId(agrs[0]);
                                    cities1.add(c);
                                }
                                DataSupport.saveAll(cities1);
                            }
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<City>>() {
                            @Override
                            public void accept(@NonNull List<City> cities) throws Exception {
                                Log.e("TAG", cities.toString());
                                queryCities(agrs[0]);
                                closeProgressDialog();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                closeProgressDialog();
//                                Toast.makeText(getActivity(), "请求数据失败", Toast.LENGTH_SHORT).show();

                                Snackbar.make(ChooseFragment.this.getView(), "请求数据失败", 5000)
                                        .setAction("重新加载", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                queryCities(selecttedProvince.getProvinceCode());
                                            }
                                        }).show();
                            }
                        });
                dis.add(subscribe1);
                break;
            case "county":
                Disposable subscribe2 = Utility.getCoolWeatherAPIService()
                        .queryCountys(agrs[0], agrs[1])
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnNext(new Consumer<List<County>>() {
                            @Override
                            public void accept(@NonNull List<County> counties) throws Exception {
                                ArrayList<County> counties1 = new ArrayList<>();
                                for (County c : counties) {
                                    Log.e("county", c.getName());
                                    c.setCityId(agrs[1]);
                                    counties1.add(c);
                                }

                                DataSupport.saveAll(counties1);
                            }
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<County>>() {
                            @Override
                            public void accept(@NonNull List<County> counties) throws Exception {
                                Log.e("TAG", counties.toString());
                                queryCounties(agrs[0], agrs[1]);
                                closeProgressDialog();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                closeProgressDialog();
//                                Toast.makeText(getActivity(), "请求数据失败", Toast.LENGTH_SHORT).show();
                                Snackbar.make(ChooseFragment.this.getView(), "请求数据失败", 5000)
                                        .setAction("重新加载", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                queryCounties(selecttedProvince.getProvinceCode(), selectedCity.getCityCode());
                                            }
                                        }).show();
                            }
                        });
                dis.add(subscribe2);
                break;
        }
    }

    /**
     * 显示对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dis.clear();
    }
}
