
package com.android.util;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class SwingAnimation extends Animation {

    //Camera camera = new Camera();

    int mCenterX;

    int mCenterY;

    public SwingAnimation() {

    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final Matrix matrix = t.getMatrix();
        if(true){
//        	float dx = -2 + 2 * interpolatedTime;
//        	android.util.Log.w("QsLog", "applyTransformation===interpolatedTime:"+interpolatedTime
//        			+"==dx:"+dx);
//        	
        	matrix.setRotate(2.0f * (1.0f -  interpolatedTime));//.setRotate(2 - 2 * interpolatedTime, mCenterX, mCenterY);
        	//matrix.set.setTranslate(dx, 0);
        } else {
	        matrix.setTranslate(-2 + 4 * interpolatedTime, 0);
	        // camera.save();
	        // camera.rotateZ(-5 + 10 * interpolatedTime);
	        // camera.getMatrix(matrix);
	        // matrix.preRotate(-5 + 10 * interpolatedTime);
	        matrix.setRotate(3 - 3 * interpolatedTime, mCenterX, mCenterY);
	        // matrix.preTranslate(-mCenterX, -mCenterY);
	        // matrix.postTranslate(mCenterX, mCenterY);
	        // camera.restore();
        }
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCenterX = width / 2;
        mCenterY = height / 2;
        setDuration(120);
        setRepeatCount(INFINITE);
        setRepeatMode(REVERSE);
        setInterpolator(new LinearInterpolator());
    }
}
