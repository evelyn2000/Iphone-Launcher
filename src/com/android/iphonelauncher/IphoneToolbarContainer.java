
package com.android.iphonelauncher;

import com.android.ui.IphoneShortcutCallback;
import com.android.util.AnimManager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

public class IphoneToolbarContainer extends ViewGroup {

    private static final String TAG = "QsPageView";

    private Scroller mScroller;

    private VelocityTracker mVelocityTracker;

    private int mCurScreen;

    private int mDefaultScreen = 0;

    private static final int TOUCH_STATE_REST = 0;

    private static final int TOUCH_STATE_SCROLLING = 1;

    private static final int SNAP_VELOCITY = 150;

    private int mTouchState = TOUCH_STATE_REST;

    private int mTouchSlop;

    private float mLastMotionX;

    private float mLastMotionY;

    private int mMaximumVelocity;

    private static final int INVALID_POINTER = -1;

    private int mActivePointerId = INVALID_POINTER;

    private boolean isScrollMode = true;

    private WorkspaceOvershootInterpolator mScrollInterpolator;

    private static final float BASELINE_FLING_VELOCITY = 2500.f;

    private static final float FLING_VELOCITY_INFLUENCE = 0.06f;

    //
    private static final int MAX_TASKS_NUM = 100;

    private ActivityManager mAm;

    private List<RunningTaskInfo> mTaskInfos;

    private LayoutInflater mInflater;

    private Launcher mLauncher;

    private View.OnClickListener mClickListener;

    private static class WorkspaceOvershootInterpolator implements Interpolator {
        private static final float DEFAULT_TENSION = 1.3f;

        private float mTension;

        public WorkspaceOvershootInterpolator() {
            mTension = DEFAULT_TENSION;
        }

        public void setDistance(int distance) {
            mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;
        }

        public void disableSettle() {
            mTension = 0.f;
        }

        public float getInterpolation(float t) {
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }

    public IphoneToolbarContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IphoneToolbarContainer(Context context, boolean isScrollMode) {
        super(context);
        this.isScrollMode = isScrollMode;
        init();
    }

    public IphoneToolbarContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setIsScrollMode(boolean isScrollMode) {
        this.isScrollMode = isScrollMode;
    }

    private void init() {
        mScrollInterpolator = new WorkspaceOvershootInterpolator();
        mScroller = new Scroller(mContext, mScrollInterpolator);
        mCurScreen = mDefaultScreen;
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();

        //
        mAm = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        mInflater = LayoutInflater.from(getContext());
    }

    public void initRunningTasks() {
        getHandler().post(new Runnable() {
            public void run() {
                mTaskInfos = mAm.getRunningTasks(MAX_TASKS_NUM);
                final int N = mTaskInfos.size();
                if (N <= 0) {
                    return;
                }
                removeAllViewsInLayout();
                // init celllayout
                int cell_nums = N / 4 + ((N % 4) > 0 ? 1 : 0);
                for (int i = 0; i < cell_nums; i++) {
                    CellLayout layout = (CellLayout) mInflater.inflate(
                            R.layout.iphone_toolbar_screen, IphoneToolbarContainer.this, false);
                    addView(layout, -1);
                }

                // add task icon
                for (int i = 0; i < N; i++) {
                    RunningTaskInfo info = mTaskInfos.get(i);
                    // if
                    // (info.baseActivity.getPackageName().equals(mContext.getPackageName()))
                    // {
                    // // ignore my task
                    // continue;
                    // }
                    View taskIcon = null;//mLauncher.createIphoneTaskIcon(info);
                    if (taskIcon == null) {
                        continue;
                    }
                    for (int j = 0; j < cell_nums; j++) {
                        CellLayout layout = (CellLayout) getChildAt(j);
                        int[] vacant = new int[2];
                        if (layout.getVacantCell(vacant, 1, 1)) {
                            addInScreen(taskIcon, j, vacant[0], vacant[1], 1, 1, false);
                            taskIcon.setOnClickListener(mClickListener);
                            break;
                        }
                    }
                }
            }
        });

    }

    void addInScreen(View child, int screen, int x, int y, int spanX, int spanY, boolean insert) {
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }
        final CellLayout group = (CellLayout) getChildAt(screen);
        group.addView(child, insert ? 0 : -1, lp);
        child.setHapticFeedbackEnabled(false);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                final int childWidth = childView.getMeasuredWidth();
                childView.layout(childLeft, 0, childLeft + childWidth,
                        childView.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ScrollLayout only canmCurScreen run at EXACTLY mode!");
        }
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
        }
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        scrollTo(mCurScreen * width, 0);
    }

    /**
     * According to the position of current layout scroll to the destination
     * page.
     */

    // public void snapToDestination() {
    // final int screenWidth = getWidth();
    // final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
    // snapToScreen(destScreen);
    // }
    //
    // public void snapToScreen(int whichScreen) {
    // // get the valid layout page
    // whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
    // if (getScrollX() != (whichScreen * getWidth())) {
    // final int delta = whichScreen * getWidth() - getScrollX();
    // mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
    // mCurScreen = whichScreen;
    // invalidate(); // Redraw the layout
    // }
    // }

    // public void setToScreen(int whichScreen) {
    // whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
    // mCurScreen = whichScreen;
    // scrollTo(whichScreen * getWidth(), 0);
    // }

    public int getCurScreen() {
        return mCurScreen;
    }

    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mTouchState = TOUCH_STATE_SCROLLING;
                mLastMotionX = event.getX();
                mActivePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    final int pointerIndex = event.findPointerIndex(mActivePointerId);
                    final float x = event.getX(pointerIndex);
                    float deltaX = mLastMotionX - x;
                    mLastMotionX = x;
                    if (deltaX < 0) {
                        if (mScrollX > 0) {
                            scrollBy((int) Math.max(-mScrollX, deltaX), 0);
                        }
                    } else if (deltaX > 0) {
                        final int availableToScroll = getChildAt(getChildCount() - 1).getRight()
                                - mScrollX - getWidth();
                        if (availableToScroll > 0) {

                            scrollBy((int) Math.min(availableToScroll, deltaX), 0);
                        }
                    } else {
                        awakenScrollBars();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    final int velocityX = (int) velocityTracker.getXVelocity(mActivePointerId);
                    if (isScrollMode) {
                        final int screenWidth = getWidth();
                        final int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;
                        final float scrolledPos = (float) mScrollX / screenWidth;

                        if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
                            // Fling hard enough to move left.
                            // Don't fling across more than one screen at a
                            // time.
                            final int bound = scrolledPos < whichScreen ? mCurScreen - 1
                                    : mCurScreen;
                            snapToScreen(Math.min(whichScreen, bound), velocityX, true);
                        } else if (velocityX < -SNAP_VELOCITY && mCurScreen < getChildCount() - 1) {
                            // Fling hard enough to move right
                            // Don't fling across more than one screen at a
                            // time.
                            final int bound = scrolledPos > whichScreen ? mCurScreen + 1
                                    : mCurScreen;
                            snapToScreen(Math.max(whichScreen, bound), velocityX, true);
                        } else {
                            snapToScreen(whichScreen, 0, true);
                        }
                    } else {
                        int max = (getChildCount() - 1) * getWidth();
                        if (velocityX > SNAP_VELOCITY) {
                            mScroller.fling(mScrollX, 0, -velocityX * 2 / 3, 0, 0, max, 0, 0);
                        } else if (velocityX < -SNAP_VELOCITY) {
                            mScroller.fling(mScrollX, 0, -velocityX * 2 / 3, 0, 0, max, 0, 0);
                        }
                        if (mVelocityTracker != null) {
                            mVelocityTracker.recycle();
                            mVelocityTracker = null;
                        }
                    }
                }
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
        }
        return true;

    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                if (xMoved) {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
            }
                break;
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                mLastMotionX = x;
                mLastMotionY = y;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
            }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = ev.getX(newPointerIndex);
            mLastMotionY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void snapToScreen(int whichScreen, int velocity, boolean settle) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));

        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurScreen
                && focusedChild == getChildAt(mCurScreen)) {
            focusedChild.clearFocus();
        }

        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurScreen));
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        int duration = (screenDelta + 1) * 100;

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        mCurScreen = whichScreen;

        if (settle) {
            mScrollInterpolator.setDistance(screenDelta);
        } else {
            mScrollInterpolator.disableSettle();
        }

        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration += (duration / (velocity / BASELINE_FLING_VELOCITY))
                    * FLING_VELOCITY_INFLUENCE;
        } else {
            duration += 32;
        }

        awakenScrollBars(duration);
        mScroller.startScroll(mScrollX, 0, delta, 0, duration);
        invalidate();
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public void setIconClickListener(View.OnClickListener listener) {
        mClickListener = listener;
    }
}
