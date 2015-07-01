
package com.android.util;

import com.android.iphonelauncher.Launcher;
import com.android.iphonelauncher.R;
import com.android.iphonelauncher.Utilities;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;

import java.util.ArrayList;

public class Common {

    private static final String TAG = "IphoneLauncher.Common";

    public static ComponentName[] loadNavigates(Launcher launcher) {
        String[] mHotseatConfig = null;
        ArrayList<ComponentName> componentNames = null;
        if (mHotseatConfig == null) {
            mHotseatConfig = launcher.getResources().getStringArray(R.array.iphone_navigate);
            if (mHotseatConfig.length > 0) {
                componentNames = new ArrayList<ComponentName>();
            }
        }
        for (int i = 0; i < mHotseatConfig.length; i++) {
            Intent intent = null;
            try {
                intent = Intent.parseUri(mHotseatConfig[i], 0);
                componentNames.add(intent.getComponent());
            } catch (java.net.URISyntaxException ex) {

            }
        }
        return componentNames.toArray(new ComponentName[componentNames.size()]);
    }

    public static Bitmap makeGaryBitmap(Bitmap bitmap, Context context) {
        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();
        final Bitmap garyBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        final Canvas canvas = new Canvas(garyBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        paint.setAlpha(100);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return garyBitmap;
    }
}
