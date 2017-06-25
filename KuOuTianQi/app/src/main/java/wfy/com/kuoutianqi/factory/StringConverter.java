package wfy.com.kuoutianqi.factory;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;


public class StringConverter<T> implements Converter<ResponseBody, T> {
    @Override
    public T convert(ResponseBody value) throws IOException {
        return (T) value.string();
    }

}
