package wfy.com.kuoutianqi.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AutoSwipeRefreshLayout extends SwipeRefreshLayout {
    public AutoSwipeRefreshLayout(Context context) {
        super(context);
    }

    public AutoSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 因为SwipeRefreshLayout 直接调用setRefresh方法是不会在动画结束时回调监听的；
     * setRefresh方法只是显示mCircleView进度条，并不会修改mNotify，只有mNotify为true才会回调监听
     */
    public void autoSwipeRefresh() {
        try {
            Class<SwipeRefreshLayout> aClass = SwipeRefreshLayout.class;
            Field mCircleView = aClass.getDeclaredField("mCircleView");
            mCircleView.setAccessible(true);
            View mProgressView = (View) mCircleView.get(this);
            mProgressView.setVisibility(View.VISIBLE);

            Method method = aClass.getDeclaredMethod("setRefreshing", boolean.class, boolean.class);
            method.setAccessible(true);
            method.invoke(this, true, true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
