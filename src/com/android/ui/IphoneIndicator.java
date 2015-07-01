
package com.android.ui;

import com.android.iphonelauncher.R;

import android.R.integer;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class IphoneIndicator extends ImageView {

    static final int INDICATOR_SPACE = 10;

    int padding_left;

    int count;

    int index;

    Drawable indicator_focus;

    Drawable indicator_unfocus;

    Drawable indicator_search_focus;

    Drawable indicator_search_unfocus;

    int indicator_size;

    boolean mPortrait = true;

    public IphoneIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initRes();
    }

    private void initRes() {
        indicator_focus = getResources().getDrawable(R.drawable.indicator_focus);
        indicator_unfocus = getResources().getDrawable(R.drawable.indicator_unfocus);
        indicator_search_focus = getResources().getDrawable(R.drawable.indicator_search_focus);
        indicator_search_unfocus = getResources().getDrawable(R.drawable.indicator_search_unfocus);

        indicator_size = getResources().getDimensionPixelSize(R.dimen.iphone_indicator_size);

        mPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public void setCountAndIndex(int count, int index) {
        this.count = count;
        this.index = index;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        if (mPortrait) {
            padding_left = (dm.widthPixels - count * indicator_size - (count - 1) * INDICATOR_SPACE) / 2;

        } else {
            padding_left = (dm.heightPixels - (int) (25 * dm.density) - count * indicator_size - (count - 1)
                    * INDICATOR_SPACE) / 2;
        }
        invalidate();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (count > 0 && index >= 0) {
            for (int i = 0; i < count; i++) {
                Drawable drawable = null;
                if (i == 0) {
                    if (index == i) {
                        drawable = indicator_search_focus;
                    } else {
                        drawable = indicator_search_unfocus;
                    }
                } else {
                    if (index == i) {
                        drawable = indicator_focus;
                    } else {
                        drawable = indicator_unfocus;
                    }
                }
                if (drawable != null) {
                    drawable.setBounds(0, 0, indicator_size, indicator_size);
                    int offset = padding_left + i * (indicator_size + INDICATOR_SPACE);
                    if (mPortrait) {
                        canvas.translate(offset, 0);
                        drawable.draw(canvas);
                        canvas.translate(-offset, 0);
                    } else {
                        canvas.translate(0, offset);
                        drawable.draw(canvas);
                        canvas.translate(0, -offset);
                    }
                }
            }
        }
    }
}
