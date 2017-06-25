package wfy.com.kuoutianqi;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import wfy.com.kuoutianqi.db.Province;
import wfy.com.kuoutianqi.util.Utility;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("wfy.com.kuoutianqi", appContext.getPackageName());
    }

    @Test
    public void textApi() {
        Utility.getCoolWeatherAPIService()
                .queryProvinces()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<List<Province>, ObservableSource<Province>>() {
                    @Override
                    public ObservableSource<Province> apply(@NonNull List<Province> provinces) throws Exception {
                        return Observable.fromIterable(provinces);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Province>() {
                    @Override
                    public void accept(@NonNull Province province) throws Exception {
                        Log.e("HAHAHA", province.getName());
                    }
                });
    }
}
