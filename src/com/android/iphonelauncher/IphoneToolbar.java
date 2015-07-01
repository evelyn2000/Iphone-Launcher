
package com.android.iphonelauncher;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class IphoneToolbar extends RelativeLayout implements View.OnClickListener {

    private static final String TAG = "IphoneToolbar";

    private Launcher mLauncher;

    private boolean mOpened = false;

    private Animation mUpAnimation;

    private Animation mDownAnimation;

    private IphoneToolbarContainer mContainer;

    private ImageView mMask;

    // private ImageView mIcon;

    public IphoneToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAnimation();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //mContainer = (IphoneToolbarContainer) findViewById(R.id.toobar_container);
        mContainer.setIconClickListener(this);
        //mMask = (ImageView) findViewById(R.id.toobar_mask);
        mMask.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                close();
            }
        });
    }

    private void initAnimation() {
        final int yOffset = getResources().getDimensionPixelSize(R.dimen.workspace_cell_height);
        mUpAnimation = new TranslateAnimation(0, 0, yOffset, 0);
        mUpAnimation.setRepeatCount(0);
        mUpAnimation.setDuration(300);
        // mUpAnimation.setAnimationListener(new AnimationListener() {
        // public void onAnimationStart(Animation animation) {
        // }
        //
        // public void onAnimationRepeat(Animation animation) {
        // }
        //
        // public void onAnimationEnd(Animation animation) {
        // final int yOffset = getResources().getDimensionPixelSize(
        // R.dimen.workspace_cell_height);
        // mMask.layout(0, -yOffset, mMask.getWidth(), mMask.getHeight() -
        // yOffset);
        // }
        // });
        mUpAnimation.setInterpolator(new LinearInterpolator());

        mDownAnimation = new TranslateAnimation(0, 0, 0, yOffset);
        mDownAnimation.setRepeatCount(0);
        mDownAnimation.setDuration(300);
        mDownAnimation.setInterpolator(new LinearInterpolator());
        // mDownAnimation.setAnimationListener(new AnimationListener() {
        // public void onAnimationStart(Animation animation) {
        // }
        //
        // public void onAnimationRepeat(Animation animation) {
        // }
        //
        // public void onAnimationEnd(Animation animation) {
        //
        // }
        // });
    }

    public void open() {
        if (mOpened) {
            return;
        }
        mOpened = true;
        mMask.setImageBitmap(makeMaskBitmap());
        mContainer.initRunningTasks();
        mContainer.setVisibility(View.VISIBLE);
        mMask.setVisibility(View.VISIBLE);
        startAnimation(true);
    }

    public void close() {
        if (!mOpened) {
            return;
        }
        mOpened = false;
        mContainer.setVisibility(View.GONE);
        mMask.setVisibility(View.GONE);
        startAnimation(false);
    }

    private void startAnimation(boolean up) {
        final int height = getResources().getDimensionPixelSize(R.dimen.workspace_cell_height);
        Animation animation = up ? mUpAnimation : mDownAnimation;

        mContainer.startAnimation(animation);
        mMask.setAnimation(animation);

        LayoutParams lp = (LayoutParams) mMask.getLayoutParams();
        lp.topMargin = up ? -height : 0;
    }

    public boolean isOpen() {
        return mOpened;
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
        mContainer.setLauncher(mLauncher);
    }

    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
            close();
            final Intent intent = ((ShortcutInfo) tag).intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0] + v.getWidth(), pos[1]
                    + v.getHeight()));
            mLauncher.startActivitySafely(intent, tag);
        }
    }

    private Bitmap makeMaskBitmap() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (width <= 0 || height <= 0) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            width = dm.widthPixels;
            height = (int) (dm.heightPixels - 25 * dm.density);
        }
        final Bitmap maskBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        final Canvas canvas = new Canvas(maskBitmap);
        final DragLayer dragLayer = mLauncher.getDragLayer();
        WallpaperManager wm = WallpaperManager.getInstance(getContext());
        final Bitmap wallpaper = ((BitmapDrawable) wm.getDrawable()).getBitmap();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int screen = mLauncher.getCurrentWorkspaceScreen();
        Display display = mLauncher.getWindowManager().getDefaultDisplay();
        final int offset = (wallpaper.getWidth() - display.getWidth())
                / (mLauncher.getWorkspace().getChildCount() - 1) * screen;
        Rect src = new Rect(offset, (int) (25 * dm.density), offset + display.getWidth(),
                wallpaper.getHeight());
        Rect dst = new Rect(0, 0, width, height);
        canvas.drawBitmap(wallpaper, src, dst, null);
        dragLayer.draw(canvas);
        return maskBitmap;
    }

    // public static class LayoutParams extends RelativeLayout.LayoutParams {
    // public int x;
    //
    // public int y;
    //
    // public LayoutParams(int x, int y) {
    // super(android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
    // android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
    // this.x = x;
    // this.y = y;
    // }
    //
    // public LayoutParams(Context context, AttributeSet attributeSet) {
    // super(context, attributeSet);
    // }
    //
    // public LayoutParams(ViewGroup.LayoutParams layoutParams) {
    // super(layoutParams);
    // }
    // }
    //
    // protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams)
    // {
    // return layoutParams instanceof LayoutParams;
    // }
    //
    // protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    // return new LayoutParams(p);
    // }
    //
    // public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
    // return new LayoutParams(getContext(), attributeSet);
    // }
    //
    // protected void onLayout(boolean bool, int l, int r, int t, int b) {
    // final int N = getChildCount();
    // for (int i = 0; i < N; i++) {
    // View child = getChildAt(i);
    // if (child.getVisibility() != View.GONE) {
    // LayoutParams lp = (LayoutParams) child.getLayoutParams();
    // child.layout(lp.x, lp.y, lp.width + lp.x, lp.height + lp.y);
    // Log.d(TAG, lp.x + "," + lp.y + "," + lp.width + "," + lp.height);
    // }
    // }
    // }
    //
    // protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // // int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    // int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
    // // int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
    // int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
    //
    // final int height =
    // getResources().getDimensionPixelSize(R.dimen.workspace_cell_height);
    //
    // LayoutParams containerLp = (LayoutParams) mContainer.getLayoutParams();
    // containerLp.width = widthSpecSize;
    // containerLp.height = height;
    // containerLp.x = 0;
    // containerLp.y = heightSpecSize - height;
    //
    // mContainer.measure(widthMeasureSpec, heightMeasureSpec);
    //
    // LayoutParams maskLp = (LayoutParams) mMask.getLayoutParams();
    // maskLp.width = widthSpecSize;
    // maskLp.height = heightSpecSize;
    // mMask.measure(widthMeasureSpec, heightMeasureSpec);
    //
    // setMeasuredDimension(widthSpecSize, heightSpecSize);
    // }
}
