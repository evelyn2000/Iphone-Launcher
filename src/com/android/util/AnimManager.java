
package com.android.util;

import com.android.iphonelauncher.IphoneInstallAppShortCut;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import java.util.ArrayList;

public class AnimManager {

    private ArrayList<View> mAnimControllers;
    private final static int MAX_ANIMATION_COUNT = 4;
    private SwingAnimation[] animation = new SwingAnimation[MAX_ANIMATION_COUNT];

    private static AnimManager mAnimManager = null;

    private boolean mIsAnim = false;

    public static AnimManager getInstance() {
        if (mAnimManager == null) {
            mAnimManager = new AnimManager();
        }
        return mAnimManager;
    }

    public AnimManager() {
        mAnimControllers = new ArrayList<View>();
        for(int i=0; i<MAX_ANIMATION_COUNT; i++)
        	animation[i] = new SwingAnimation();
    }

    public void addControllers(View view) {
        if (!mAnimControllers.contains(view)) {
            mAnimControllers.add(view);
        }
    }

    public void start() {
        if (mIsAnim) {
            stop();
        }
        mIsAnim = true;
        final int N = mAnimControllers.size();
        for (int i = 0; i < N; i++) {
            View view = mAnimControllers.get(i);
            view.startAnimation(animation[i%MAX_ANIMATION_COUNT]);
            if (view instanceof IphoneInstallAppShortCut) {
                // need show the uninstall btn
                ((IphoneInstallAppShortCut) view).showUninstallBtn(true);
            }
        }
    }

    public void startSingle(View view) {
        if (!mAnimControllers.contains(view)) {
            mAnimControllers.add(view);
        }
        if (mIsAnim) {
            view.startAnimation(animation[0]);
            if (view instanceof IphoneInstallAppShortCut) {
                // need show the uninstall btn
                ((IphoneInstallAppShortCut) view).showUninstallBtn(true);
            }
        }
    }

    public void stop() {
        if (!mIsAnim) {
            return;
        }
        mIsAnim = false;
        final int N = mAnimControllers.size();
        for (int i = 0; i < N; i++) {
            final View view = mAnimControllers.get(i);
            view.clearAnimation();
            if (view instanceof IphoneInstallAppShortCut) {
                // need hide the uninstall btn
                ((IphoneInstallAppShortCut) view).showUninstallBtn(false);
            }
        }
    }

    public boolean isAnim() {
        return mIsAnim;
    }

    public void removeControllers(View view) {
        mAnimControllers.remove(view);
    }
}
