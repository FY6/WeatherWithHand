package wfy.com.kuoutianqi.factory;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;


public class StringConverterFactory extends Converter.Factory {

    private StringConverterFactory() {
    }

    public static StringConverterFactory create() {
        return new StringConverterFactory();
    }

    /**
     * @param type
     * @param annotations 这些注解是在ApiSerevr中定义的
     * @param retrofit    和我们在activit创建的是同一个对象
     * @return
     */
    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {

        Log.e("requestBodyConverter", "type: " + TypeToken.get(type).getRawType().getSimpleName());
        for (Annotation an : annotations) {
            Log.e("response paraAnnotation", an.annotationType().getSimpleName());
        }

        Log.e("response retrofit", retrofit.toString());
        if (type != String.class) {
            return null;
        }
        return new StringConverter<String>();
    }

    /**
     * @param type                 这个类型是根据apiserver中的定义的一致
     * @param parameterAnnotations 参数的注解
     * @param methodAnnotations    方法的注解，一般都在ApiServer中定义
     * @param retrofit
     * @return
     */
    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        Log.e("requestBodyConverter", "type: " + type.getClass().getSimpleName());

        Log.e("sixe", parameterAnnotations.length + "  methodAnnotations" + methodAnnotations.length);
        for (Annotation an : parameterAnnotations) {
            Log.e("request paramAnnotation", an.annotationType().getSimpleName());
        }
        for (Annotation an : methodAnnotations) {
            Log.e("request me Annotation", an.annotationType().getSimpleName());
        }
        Log.e("re retrofit", retrofit.toString());
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }

    @Nullable
    @Override
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return super.stringConverter(type, annotations, retrofit);
    }
}
