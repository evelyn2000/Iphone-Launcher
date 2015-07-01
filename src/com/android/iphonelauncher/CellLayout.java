/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.iphonelauncher;

import com.android.ui.CellLayoutConfig;
import com.android.util.AnimManager;

import android.R.integer;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import java.util.ArrayList;

public class CellLayout extends ViewGroup {
    private static final String TAG = "Launcher2.CellLayout";
    
    public final static int LAYOUT_ORIENTATION_AUTO = 0;
    public final static int LAYOUT_ORIENTATION_PORTRAIT = 1;
    public final static int LAYOUT_ORIENTATION_LANDSCAPE = 2;
    static final boolean DEBUG_FLAG = false;

    //private boolean mPortrait = true;

    int mCellWidth;

    int mCellHeight;

    int mLongAxisStartPadding;

    int mLongAxisEndPadding;

    int mShortAxisStartPadding;

    int mShortAxisEndPadding;

    int mShortAxisCells;

    int mLongAxisCells;

    int mWidthGap;

    int mHeightGap;

    private final Rect mRect = new Rect();

    private final CellInfo mCellInfo = new CellInfo();

    int[] mCellXY = new int[2];

    boolean[][] mOccupied;

    private RectF mDragRect = new RectF();

    private boolean mDirtyTag;

    private boolean mLastDownOnOccupiedCell = false;

    private WallpaperManager mWallpaperManager;

    private boolean mIsChildAnim = false;

    private boolean mIsInNavigate = false;

    private boolean mOrientationPortrait;
    
    public int mMaxShortAxisCells;

    public int mMaxLongAxisCells;

    // int mNavigatePaddingLeft = 0;
    public boolean isNavigate(){
    	return mIsInNavigate;
    }
    public CellLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellLayout, defStyle, 0);

        mCellWidth = a.getDimensionPixelSize(R.styleable.CellLayout_cellWidth, 10);
        mCellHeight = a.getDimensionPixelSize(R.styleable.CellLayout_cellHeight, 10);

        mLongAxisStartPadding = a.getDimensionPixelSize(
                R.styleable.CellLayout_longAxisStartPadding, 10);
        mLongAxisEndPadding = a
                .getDimensionPixelSize(R.styleable.CellLayout_longAxisEndPadding, 10);
        mShortAxisStartPadding = a.getDimensionPixelSize(
                R.styleable.CellLayout_shortAxisStartPadding, 10);
        mShortAxisEndPadding = a.getDimensionPixelSize(R.styleable.CellLayout_shortAxisEndPadding,
                10);

        mShortAxisCells = a.getInt(R.styleable.CellLayout_shortAxisCells, 4);
        mLongAxisCells = a.getInt(R.styleable.CellLayout_longAxisCells, 4);
        
        mMaxShortAxisCells = a.getInt(R.styleable.CellLayout_maxShortAxisCells, mShortAxisCells);
        mMaxLongAxisCells = a.getInt(R.styleable.CellLayout_maxLongAxisCells, mLongAxisCells);

        mIsInNavigate = a.getBoolean(R.styleable.CellLayout_isInNavigate, false);
        
        int orient = a.getInt(R.styleable.CellLayout_layoutOrientation, LAYOUT_ORIENTATION_AUTO);
        if(orient == LAYOUT_ORIENTATION_AUTO)
        	mOrientationPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        else
        	mOrientationPortrait = (orient == LAYOUT_ORIENTATION_PORTRAIT ? true : false);
        
        a.recycle();

        init();
    }

    public CellLayout(Context context, CellLayoutConfig config) {
        super(context);
        mCellWidth = config.mCellWidth;
        mCellHeight = config.mCellHeight;
        mLongAxisStartPadding = config.mLongAxisStartPadding;
        mLongAxisEndPadding = config.mLongAxisEndPadding;
        mShortAxisStartPadding = config.mShortAxisStartPadding;
        mShortAxisEndPadding = config.mShortAxisEndPadding;
        mShortAxisCells = config.mShortAxisCells;
        mLongAxisCells = config.mLongAxisCells;
        mOrientationPortrait = config.mOrientationPortrait;
        
        mMaxShortAxisCells = config.mMaxShortAxisCells;
        mMaxLongAxisCells = config.mMaxLongAxisCells;
        
        init();
    }

    private void init() {
        // setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(false);
        
        if (mOccupied == null) {
            if (mOrientationPortrait) {
                mOccupied = new boolean[mShortAxisCells][mLongAxisCells];
            } else {
                mOccupied = new boolean[mLongAxisCells][mShortAxisCells];
            }
        }

        //mWallpaperManager = WallpaperManager.getInstance(getContext());
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        // Cancel long press for all children
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.cancelLongPress();
        }
    }
    
    int getMaxCountX() {
        return mOrientationPortrait ? mMaxShortAxisCells : mMaxLongAxisCells;
    }

    int getMaxCountY() {
        return mOrientationPortrait ? mMaxLongAxisCells : mMaxShortAxisCells;
    }

    int getCountX() {
        return mOrientationPortrait ? mShortAxisCells : mLongAxisCells;
    }

    int getCountY() {
        return mOrientationPortrait ? mLongAxisCells : mShortAxisCells;
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        // Generate an id for each view, this assumes we have at most 256x256
        // cells
        // per workspace screen
        final LayoutParams cellParams = (LayoutParams) params;
        cellParams.regenerateId = true;

//        if (!mIsInNavigate) {
//            cellParams.navigate_padding = 0;
//        } else {
//            final int N = getChildCount();
//            int spaceLeft;
//            if (mOrientationPortrait) {
//                spaceLeft = (getWidth() - (mCellWidth * (N + 1) + mWidthGap * (N + 2))) / 2;
//            } else {
//                spaceLeft = (getHeight() - (mCellHeight * (N + 1) + mHeightGap * (N + 2))) / 2;
//            }
//            for (int i = 0; i < N; i++) {
//                View c = getChildAt(i);
//                LayoutParams lp = (LayoutParams) c.getLayoutParams();
//                if (lp.navigate_padding > spaceLeft) {
//                    lp.navigate_padding = spaceLeft;
//                }
//            }
//            if (mOrientationPortrait) {
//                cellParams.x = spaceLeft + cellParams.cellX * mCellWidth;
//            } else {
//                cellParams.y = spaceLeft + cellParams.cellY * mCellHeight;
//            }
//            cellParams.navigate_padding = spaceLeft;
//        }
        super.addView(child, index, params);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (child != null) {
            Rect r = new Rect();
            child.getDrawingRect(r);
            requestRectangleOnScreen(r);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mCellInfo.screen = ((ViewGroup) getParent()).indexOfChild(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        final CellInfo cellInfo = mCellInfo;
        if (action == MotionEvent.ACTION_DOWN) {
            final Rect frame = mRect;
            final int x = (int) ev.getX() + mScrollX;
            final int y = (int) ev.getY() + mScrollY;
            final int count = getChildCount();

            boolean found = false;
            for (int i = count - 1; i >= 0; i--) {
                final View child = getChildAt(i);

                if ((child.getVisibility()) == VISIBLE || child.getAnimation() != null) {
                    child.getHitRect(frame);
                    if (frame.contains(x, y)) {
                        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                        cellInfo.cell = child;
                        cellInfo.cellX = lp.cellX;
                        cellInfo.cellY = lp.cellY;
                        cellInfo.spanX = lp.cellHSpan;
                        cellInfo.spanY = lp.cellVSpan;
                        cellInfo.valid = true;
                        found = true;
                        mDirtyTag = false;
                        break;
                    }
                }
            }

            mLastDownOnOccupiedCell = found;

            if (!found) {
                int cellXY[] = mCellXY;
                pointToCellExact(x, y, cellXY);

                final boolean portrait = mOrientationPortrait;
                final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
                final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

                final boolean[][] occupied = mOccupied;
                findOccupiedCells(xCount, yCount, occupied, null);

                cellInfo.cell = null;
                cellInfo.cellX = cellXY[0];
                cellInfo.cellY = cellXY[1];
                cellInfo.spanX = 1;
                cellInfo.spanY = 1;
                cellInfo.valid = cellXY[0] >= 0 && cellXY[1] >= 0 && cellXY[0] < xCount
                        && cellXY[1] < yCount && !occupied[cellXY[0]][cellXY[1]];

                // Instead of finding the interesting vacant cells here, wait
                // until a
                // caller invokes getTag() to retrieve the result. Finding the
                // vacant
                // cells is a bit expensive and can generate many new objects,
                // it's
                // therefore better to defer it until we know we actually need
                // it.

                mDirtyTag = true;
            }
            setTag(cellInfo);
        } else if (action == MotionEvent.ACTION_UP) {
            cellInfo.cell = null;
            cellInfo.cellX = -1;
            cellInfo.cellY = -1;
            cellInfo.spanX = 0;
            cellInfo.spanY = 0;
            cellInfo.valid = false;
            mDirtyTag = false;
            setTag(cellInfo);
        }

        return false;
    }

    @Override
    public CellInfo getTag() {
        final CellInfo info = (CellInfo) super.getTag();
        if (mDirtyTag && info.valid) {
            final boolean portrait = mOrientationPortrait;
            final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
            final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

            final boolean[][] occupied = mOccupied;
            findOccupiedCells(xCount, yCount, occupied, null);

            findIntersectingVacantCells(info, info.cellX, info.cellY, xCount, yCount, occupied);

            mDirtyTag = false;
        }
        return info;
    }

    private static void findIntersectingVacantCells(CellInfo cellInfo, int x, int y, int xCount,
            int yCount, boolean[][] occupied) {

        cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
        cellInfo.clearVacantCells();

        if (occupied[x][y]) {
            return;
        }

        cellInfo.current.set(x, y, x, y);

        findVacantCell(cellInfo.current, xCount, yCount, occupied, cellInfo);
    }

    private static void findVacantCell(Rect current, int xCount, int yCount, boolean[][] occupied,
            CellInfo cellInfo) {

        addVacantCell(current, cellInfo);

        if (current.left > 0) {
            if (isColumnEmpty(current.left - 1, current.top, current.bottom, occupied)) {
                current.left--;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.left++;
            }
        }

        if (current.right < xCount - 1) {
            if (isColumnEmpty(current.right + 1, current.top, current.bottom, occupied)) {
                current.right++;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.right--;
            }
        }

        if (current.top > 0) {
            if (isRowEmpty(current.top - 1, current.left, current.right, occupied)) {
                current.top--;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.top++;
            }
        }

        if (current.bottom < yCount - 1) {
            if (isRowEmpty(current.bottom + 1, current.left, current.right, occupied)) {
                current.bottom++;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.bottom--;
            }
        }
    }

    private static void addVacantCell(Rect current, CellInfo cellInfo) {
        CellInfo.VacantCell cell = CellInfo.VacantCell.acquire();
        cell.cellX = current.left;
        cell.cellY = current.top;
        cell.spanX = current.right - current.left + 1;
        cell.spanY = current.bottom - current.top + 1;
        if (cell.spanX > cellInfo.maxVacantSpanX) {
            cellInfo.maxVacantSpanX = cell.spanX;
            cellInfo.maxVacantSpanXSpanY = cell.spanY;
        }
        if (cell.spanY > cellInfo.maxVacantSpanY) {
            cellInfo.maxVacantSpanY = cell.spanY;
            cellInfo.maxVacantSpanYSpanX = cell.spanX;
        }
        cellInfo.vacantCells.add(cell);
    }

    private static boolean isColumnEmpty(int x, int top, int bottom, boolean[][] occupied) {
        for (int y = top; y <= bottom; y++) {
            if (occupied[x][y]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRowEmpty(int y, int left, int right, boolean[][] occupied) {
        for (int x = left; x <= right; x++) {
            if (occupied[x][y]) {
                return false;
            }
        }
        return true;
    }

    CellInfo findAllVacantCells(boolean[] occupiedCells, View ignoreView) {
        final boolean portrait = mOrientationPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

        boolean[][] occupied = mOccupied;

        if (occupiedCells != null) {
            for (int y = 0; y < yCount; y++) {
                for (int x = 0; x < xCount; x++) {
                    occupied[x][y] = occupiedCells[y * xCount + x];
                }
            }
        } else {
            findOccupiedCells(xCount, yCount, occupied, ignoreView);
        }

        CellInfo cellInfo = new CellInfo();

        cellInfo.cellX = -1;
        cellInfo.cellY = -1;
        cellInfo.spanY = 0;
        cellInfo.spanX = 0;
        cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
        cellInfo.screen = mCellInfo.screen;

        Rect current = cellInfo.current;

        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                if (!occupied[x][y]) {
                    current.set(x, y, x, y);
                    findVacantCell(current, xCount, yCount, occupied, cellInfo);
                    occupied[x][y] = true;
                }
            }
        }

        cellInfo.valid = cellInfo.vacantCells.size() > 0;

        // Assume the caller will perform their own cell searching, otherwise we
        // risk causing an unnecessary rebuild after findCellForSpan()

        return cellInfo;
    }

    /**
     * Given a point, return the cell that strictly encloses that point
     * 
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param result Array of 2 ints to hold the x and y coordinate of the cell
     */
    void pointToCellExact(int x, int y, int[] result) {
        final boolean portrait = mOrientationPortrait;

        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;

        result[0] = (x - hStartPadding) / (mCellWidth + mWidthGap);
        result[1] = (y - vStartPadding) / (mCellHeight + mHeightGap);

        final int xAxis = portrait ? mShortAxisCells : mLongAxisCells;
        final int yAxis = portrait ? mLongAxisCells : mShortAxisCells;

        if (result[0] < 0)
            result[0] = 0;
        if (result[0] >= xAxis)
            result[0] = xAxis - 1;
        if (result[1] < 0)
            result[1] = 0;
        if (result[1] >= yAxis)
            result[1] = yAxis - 1;
    }

    /**
     * Given a point, return the cell that most closely encloses that point
     * 
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param result Array of 2 ints to hold the x and y coordinate of the cell
     */
    void pointToCellRounded(int x, int y, int[] result) {
        pointToCellExact(x + (mCellWidth / 2), y + (mCellHeight / 2), result);
    }

    /**
     * Given a cell coordinate, return the point that represents the upper left
     * corner of that cell
     * 
     * @param cellX X coordinate of the cell
     * @param cellY Y coordinate of the cell
     * @param result Array of 2 ints to hold the x and y coordinate of the point
     */
    void cellToPoint(int cellX, int cellY, int[] result) {
        final boolean portrait = mOrientationPortrait;

        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;

        result[0] = hStartPadding + cellX * (mCellWidth + mWidthGap);
        result[1] = vStartPadding + cellY * (mCellHeight + mHeightGap);
    }

    int getCellWidth() {
        return mCellWidth;
    }

    int getCellHeight() {
        return mCellHeight;
    }

    int getLeftPadding() {
        return mOrientationPortrait ? mShortAxisStartPadding : mLongAxisStartPadding;
    }

    int getTopPadding() {
        return mOrientationPortrait ? mLongAxisStartPadding : mShortAxisStartPadding;
    }

    int getRightPadding() {
        return mOrientationPortrait ? mShortAxisEndPadding : mLongAxisEndPadding;
    }

    int getBottomPadding() {
        return mOrientationPortrait ? mLongAxisEndPadding : mShortAxisEndPadding;
    }

    int getHeightGap() {
        return mHeightGap;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO: currently ignoring padding

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
        }

        final int shortAxisCells = mShortAxisCells;
        final int longAxisCells = mLongAxisCells;
        final int longAxisStartPadding = mLongAxisStartPadding;
        final int longAxisEndPadding = mLongAxisEndPadding;
        //
        final int shortAxisStartPadding = mShortAxisStartPadding;
        //

        final int shortAxisEndPadding = mShortAxisEndPadding;
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;

        // mPortrait = heightSpecSize > widthSpecSize;
        //mPortrait = true;
        int numShortGaps = shortAxisCells + 1;
        int numLongGaps = longAxisCells + 1;

        if (mOrientationPortrait) {
            int vSpaceLeft = heightSpecSize - longAxisStartPadding - longAxisEndPadding
                    - (cellHeight * longAxisCells);
            if(numLongGaps > 0){
//	            if (vSpaceLeft < 0) {
//	            	mHeightGap = vSpaceLeft / longAxisCells;
//	            } else {
	            	mHeightGap = vSpaceLeft / numLongGaps;
	            //}
            }else{
            	mHeightGap = 0;
            }

            int hSpaceLeft = widthSpecSize - shortAxisStartPadding - shortAxisEndPadding
                    - (cellWidth * shortAxisCells);
            if (numShortGaps > 0) {
//                if (hSpaceLeft < 0) {
//                    mWidthGap = hSpaceLeft / shortAxisCells - 1;
//                } else {
                    mWidthGap = hSpaceLeft / numShortGaps;
                //}
            } else {
                mWidthGap = 0;
            }
            
        } else {
            int hSpaceLeft = widthSpecSize - longAxisStartPadding - longAxisEndPadding
                    - (cellWidth * longAxisCells);
            if(numLongGaps > 0){
//	            if (hSpaceLeft < 0) {
//	            	mWidthGap = hSpaceLeft / longAxisCells;	
//	            } else {
	            	mWidthGap = hSpaceLeft / numLongGaps;
	            //}
            }else{
            	mWidthGap = 0;
            }

            int vSpaceLeft = heightSpecSize - shortAxisStartPadding - shortAxisEndPadding
                    - (cellHeight * shortAxisCells);
            if (numShortGaps > 0) {
//            	if (vSpaceLeft < 0) {
//            		mHeightGap = vSpaceLeft / shortAxisCells - 1;
//            	} else {
            		mHeightGap = vSpaceLeft / numShortGaps;
            	//}  
            } else {
                mHeightGap = 0;
            } 
        }

        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (mOrientationPortrait) {
                lp.setup(cellWidth, cellHeight, mWidthGap, mHeightGap, shortAxisStartPadding,
                        longAxisStartPadding);
            } else {
                lp.setup(cellWidth, cellHeight, mWidthGap, mHeightGap, longAxisStartPadding,
                        shortAxisStartPadding);
            }

            if (lp.regenerateId) {
                child.setId(((getId() & 0xFF) << 16) | (lp.cellX & 0xFF) << 8 | (lp.cellY & 0xFF));
                lp.regenerateId = false;
            }

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
            int childheightMeasureSpec = MeasureSpec
                    .makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

                int childLeft = lp.x;
                int childTop = lp.y;
                child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);

                if (lp.dropped) {
                    lp.dropped = false;

//                    final int[] cellXY = mCellXY;
//                    getLocationOnScreen(cellXY);
//                    mWallpaperManager.sendWallpaperCommand(getWindowToken(), "android.home.drop",
//                            cellXY[0] + childLeft + lp.width / 2,
//                            cellXY[1] + childTop + lp.height / 2, 0, null);
                }
            }
        }
    }

    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            view.buildDrawingCache(true);
        }
    }

    @Override
    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        super.setChildrenDrawnWithCacheEnabled(enabled);
    }

    /**
     * Find a vacant area that will fit the given bounds nearest the requested
     * cell location. Uses Euclidean distance to score multiple vacant areas.
     * 
     * @param pixelX The X location at which you want to search for a vacant
     *            area.
     * @param pixelY The Y location at which you want to search for a vacant
     *            area.
     * @param spanX Horizontal span of the object.
     * @param spanY Vertical span of the object.
     * @param vacantCells Pre-computed set of vacant cells to search.
     * @param recycle Previously returned value to possibly recycle.
     * @return The X, Y cell of a vacant area that can contain this object,
     *         nearest the requested location.
     */
    int[] findNearestVacantArea(int pixelX, int pixelY, int spanX, int spanY, CellInfo vacantCells,
            int[] recycle) {

        // Keep track of best-scoring drop area
        final int[] bestXY = recycle != null ? recycle : new int[2];
        final int[] cellXY = mCellXY;
        double bestDistance = Double.MAX_VALUE;

        // Bail early if vacant cells aren't valid
        if (!vacantCells.valid) {
            return null;
        }

        // Look across all vacant cells for best fit
        final int size = vacantCells.vacantCells.size();
        for (int i = 0; i < size; i++) {
            final CellInfo.VacantCell cell = vacantCells.vacantCells.get(i);

            // Reject if vacant cell isn't our exact size
            if (cell.spanX != spanX || cell.spanY != spanY) {
                continue;
            }

            // Score is center distance from requested pixel
            cellToPoint(cell.cellX, cell.cellY, cellXY);

            double distance = Math.sqrt(Math.pow(cellXY[0] - pixelX, 2)
                    + Math.pow(cellXY[1] - pixelY, 2));
            if (distance <= bestDistance) {
                bestDistance = distance;
                bestXY[0] = cell.cellX;
                bestXY[1] = cell.cellY;
            }
        }

        // Return null if no suitable location found
        if (bestDistance < Double.MAX_VALUE) {
            return bestXY;
        } else {
            return null;
        }
    }

    /**
     * Drop a child at the specified position
     * 
     * @param child The child that is being dropped
     * @param targetXY Destination area to move to
     */
    void onDropChild(View child, int[] targetXY) {
        if (child != null) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.cellX = targetXY[0];
            lp.cellY = targetXY[1];
            lp.isDragging = false;
            lp.dropped = true;
            mDragRect.setEmpty();
            child.requestLayout();
            invalidate();
        }
    }

    void onDropAborted(View child) {
        if (child != null) {
            ((LayoutParams) child.getLayoutParams()).isDragging = false;
            invalidate();
        }
        mDragRect.setEmpty();
    }

    /**
     * Start dragging the specified child
     * 
     * @param child The child that is being dragged
     */
    void onDragChild(View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        lp.isDragging = true;
        mDragRect.setEmpty();
    }

    /**
     * Drag a child over the specified position
     * 
     * @param child The child that is being dropped
     * @param cellX The child's new x cell location
     * @param cellY The child's new y cell location
     */
    void onDragOverChild(View child, int cellX, int cellY) {
        int[] cellXY = mCellXY;
        pointToCellRounded(cellX, cellY, cellXY);
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        cellToRect(cellXY[0], cellXY[1], lp.cellHSpan, lp.cellVSpan, mDragRect);
        invalidate();
    }

    /**
     * Computes a bounding rectangle for a range of cells
     * 
     * @param cellX X coordinate of upper left corner expressed as a cell
     *            position
     * @param cellY Y coordinate of upper left corner expressed as a cell
     *            position
     * @param cellHSpan Width in cells
     * @param cellVSpan Height in cells
     * @param dragRect Rectnagle into which to put the results
     */
    public void cellToRect(int cellX, int cellY, int cellHSpan, int cellVSpan, RectF dragRect) {
        final boolean portrait = mOrientationPortrait;
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;
        final int widthGap = mWidthGap;
        final int heightGap = mHeightGap;

        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;

        int width = cellHSpan * cellWidth + ((cellHSpan - 1) * widthGap);
        int height = cellVSpan * cellHeight + ((cellVSpan - 1) * heightGap);

        int x = hStartPadding + cellX * (cellWidth + widthGap);
        int y = vStartPadding + cellY * (cellHeight + heightGap);

        dragRect.set(x, y, x + width, y + height);
    }

    /**
     * Computes the required horizontal and vertical cell spans to always fit
     * the given rectangle.
     * 
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public int[] rectToCell(int width, int height) {
        // Always assume we're working with the smallest span to make sure we
        // reserve enough space in both orientations.
        final Resources resources = getResources();
        int actualWidth = mCellWidth;//resources.getDimensionPixelSize(R.dimen.workspace_cell_width);
        int actualHeight = mCellHeight;//resources.getDimensionPixelSize(R.dimen.workspace_cell_height);
        int smallerSize = Math.min(actualWidth, actualHeight);

        // Always round up to next largest cell
        int spanX = (width + smallerSize) / smallerSize;
        int spanY = (height + smallerSize) / smallerSize;

        return new int[] {
                spanX, spanY
        };
    }
    
    private boolean checkEmptyCell(int x, int y){
    	int index = y * this.getCountX() + x;
		View child = super.getChildAt(index);
		
		if(child != null){
			ItemInfo info = (ItemInfo)child.getTag();
			if(info.cellX != x || info.cellY != y)
				return true;
			
			Log.w(Launcher.TAG, "CellLayout::isEmptyCell(no empty)==x:"+x+"==y:"+y+"==index:"+index+"=="+info.toString());
		} else {
			return true;
		}
		//Log.w(Launcher.TAG, "CellLayout::isEmptyCell(no empty)==x:"+x+"==y:"+y+"==index:"+index+"==");
		return false;
    }

    public boolean isEmptyCell(int x, int y){
    	if(/*mOccupied != null && */x >= 0 && y >= 0){
    		if(false){
    			if(x < this.getCountX() && y < this.getCountY()){
	    			return !mOccupied[x][y];
	    		}else if(isNavigate()){
	    			return !mOccupied[y][x];
	    		}
    		} else {
	    		if(x < this.getCountX() && y < this.getCountY()){
	    			return checkEmptyCell(x, y);
	    			//return (y * this.getCountX() + x) >= super.getChildCount(); //!mOccupied[x][y];
	    		}else if(isNavigate()){
	    			return checkEmptyCell(y, x);//(Math.max(x, y) >= super.getChildCount());//!mOccupied[y][x];
	    		}
    		}
    		//if(isNavigate() && (x >= this.getCountX() || y >= this.getCountY())
    	}
    	
    	return false;
    }
    /**
     * Find the first vacant cell, if there is one.
     * 
     * @param vacant Holds the x and y coordinate of the vacant cell
     * @param spanX Horizontal cell span.
     * @param spanY Vertical cell span.
     * @return True if a vacant cell was found
     */
    public boolean getVacantCell(int[] vacant, int spanX, int spanY) {
        final boolean portrait = mOrientationPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        final boolean[][] occupied = mOccupied;

        findOccupiedCells(xCount, yCount, occupied, null);

        return findVacantCell(vacant, spanX, spanY, xCount, yCount, occupied);
    }

    static boolean findVacantCell(int[] vacant, int spanX, int spanY, int xCount, int yCount,
            boolean[][] occupied) {

        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                boolean available = !occupied[x][y];
                out: for (int i = x; i < x + spanX - 1 && x < xCount; i++) {
                    for (int j = y; j < y + spanY - 1 && y < yCount; j++) {
                        available = available && !occupied[i][j];
                        if (!available)
                            break out;
                    }
                }

                if (available) {
                    vacant[0] = x;
                    vacant[1] = y;
                    return true;
                }
            }
        }

        return false;
    }

    boolean[] getOccupiedCells() {
        final boolean portrait = mOrientationPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        final boolean[][] occupied = mOccupied;

        findOccupiedCells(xCount, yCount, occupied, null);

        final boolean[] flat = new boolean[xCount * yCount];
        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                flat[y * xCount + x] = occupied[x][y];
            }
        }

        return flat;
    }

    private void findOccupiedCells(int xCount, int yCount, boolean[][] occupied, View ignoreView) {
        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                occupied[x][y] = false;
            }
        }

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof Folder || child.equals(ignoreView)) {
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            for (int x = lp.cellX; x < lp.cellX + lp.cellHSpan && x < xCount; x++) {
                for (int y = lp.cellY; y < lp.cellY + lp.cellVSpan && y < yCount; y++) {
                    occupied[x][y] = true;
                }
            }
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CellLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CellLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new CellLayout.LayoutParams(p);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        /**
         * Horizontal location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellX;

        /**
         * Vertical location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellY;

        /**
         * Number of cells spanned horizontally by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellHSpan;

        /**
         * Number of cells spanned vertically by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellVSpan;

        /**
         * Is this item currently being dragged
         */
        public boolean isDragging;

        // X coordinate of the view in the layout.
        @ViewDebug.ExportedProperty
        int x;

        // Y coordinate of the view in the layout.
        @ViewDebug.ExportedProperty
        int y;

        boolean regenerateId;

        boolean dropped;

        int navigate_padding = 0;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            cellHSpan = 1;
            cellVSpan = 1;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            cellHSpan = 1;
            cellVSpan = 1;
        }

        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
        }

        public void setup(int cellWidth, int cellHeight, int widthGap, int heightGap,
                int hStartPadding, int vStartPadding) {

            final int myCellHSpan = cellHSpan;
            final int myCellVSpan = cellVSpan;
            final int myCellX = cellX;
            final int myCellY = cellY;

            if (widthGap < 0) {
                width = myCellHSpan * cellWidth + (myCellHSpan * widthGap) - leftMargin
                        - rightMargin;
            } else {
                width = myCellHSpan * cellWidth + ((myCellHSpan - 1) * widthGap) - leftMargin
                        - rightMargin;
            }

            if (DEBUG_FLAG) {
                Log.d(TAG, "setup leftMargin==" + leftMargin + " rightMargin==" + rightMargin);
                Log.d(TAG, "setup widthGap==" + widthGap + " width==" + width);
            }

            if (heightGap < 0) {
                height = myCellVSpan * cellHeight + (myCellVSpan * heightGap) - topMargin
                        - bottomMargin;
            } else {
                height = myCellVSpan * cellHeight + ((myCellVSpan - 1) * heightGap) - topMargin
                        - bottomMargin;
            }

            if (DEBUG_FLAG) {
                Log.d(TAG, "setup topMargin==" + topMargin + " bottomMargin==" + bottomMargin);
                Log.d(TAG, "setup heightGap==" + heightGap + " height==" + height + " cellHeight=="
                        + cellHeight);
            }

            /*if (mOrientationPortrait) {
                x = hStartPadding + navigate_padding + myCellX * cellWidth + (myCellX + 1)
                        * widthGap + leftMargin;
                y = vStartPadding + myCellY * cellHeight + (myCellY + 1) * heightGap + topMargin;
            } else {
                x = hStartPadding + myCellX * cellWidth + (myCellX + 1) * widthGap + leftMargin;
                y = vStartPadding + navigate_padding + myCellY * cellHeight + (myCellY + 1)
                        * heightGap + topMargin;
            }*/
			
			x = hStartPadding + widthGap + myCellX * (cellWidth + widthGap) + leftMargin;
            y = vStartPadding + heightGap + myCellY * (cellHeight + heightGap) + topMargin;
			
            if (DEBUG_FLAG) {
                Log.d(TAG, "setup y==" + y + " topMargin==" + topMargin);
            }
        }
    }

    public static final class CellInfo implements ContextMenu.ContextMenuInfo {
        /**
         * See View.AttachInfo.InvalidateInfo for futher explanations about the
         * recycling mechanism. In this case, we recycle the vacant cells
         * instances because up to several hundreds can be instanciated when the
         * user long presses an empty cell.
         */
        static final class VacantCell {
            int cellX;

            int cellY;

            int spanX;

            int spanY;

            // We can create up to 523 vacant cells on a 4x4 grid, 100 seems
            // like a reasonable compromise given the size of a VacantCell and
            // the fact that the user is not likely to touch an empty 4x4 grid
            // very often
            private static final int POOL_LIMIT = 100;

            private static final Object sLock = new Object();

            private static int sAcquiredCount = 0;

            private static VacantCell sRoot;

            private VacantCell next;

            static VacantCell acquire() {
                synchronized (sLock) {
                    if (sRoot == null) {
                        return new VacantCell();
                    }

                    VacantCell info = sRoot;
                    sRoot = info.next;
                    sAcquiredCount--;

                    return info;
                }
            }

            void release() {
                synchronized (sLock) {
                    if (sAcquiredCount < POOL_LIMIT) {
                        sAcquiredCount++;
                        next = sRoot;
                        sRoot = this;
                    }
                }
            }

            @Override
            public String toString() {
                return "VacantCell[x=" + cellX + ", y=" + cellY + ", spanX=" + spanX + ", spanY="
                        + spanY + "]";
            }
        }

        View cell;

        int cellX;

        int cellY;

        int spanX;

        int spanY;

        int screen;

        boolean valid;

        final ArrayList<VacantCell> vacantCells = new ArrayList<VacantCell>(VacantCell.POOL_LIMIT);

        int maxVacantSpanX;

        int maxVacantSpanXSpanY;

        int maxVacantSpanY;

        int maxVacantSpanYSpanX;

        final Rect current = new Rect();

        void clearVacantCells() {
            final ArrayList<VacantCell> list = vacantCells;
            final int count = list.size();

            for (int i = 0; i < count; i++)
                list.get(i).release();

            list.clear();
        }

        void findVacantCellsFromOccupied(boolean[] occupied, int xCount, int yCount) {
            if (cellX < 0 || cellY < 0) {
                maxVacantSpanX = maxVacantSpanXSpanY = Integer.MIN_VALUE;
                maxVacantSpanY = maxVacantSpanYSpanX = Integer.MIN_VALUE;
                clearVacantCells();
                return;
            }

            final boolean[][] unflattened = new boolean[xCount][yCount];
            for (int y = 0; y < yCount; y++) {
                for (int x = 0; x < xCount; x++) {
                    unflattened[x][y] = occupied[y * xCount + x];
                }
            }
            CellLayout.findIntersectingVacantCells(this, cellX, cellY, xCount, yCount, unflattened);
        }

        /**
         * This method can be called only once! Calling
         * #findVacantCellsFromOccupied will restore the ability to call this
         * method. Finds the upper-left coordinate of the first rectangle in the
         * grid that can hold a cell of the specified dimensions.
         * 
         * @param cellXY The array that will contain the position of a vacant
         *            cell if such a cell can be found.
         * @param spanX The horizontal span of the cell we want to find.
         * @param spanY The vertical span of the cell we want to find.
         * @return True if a vacant cell of the specified dimension was found,
         *         false otherwise.
         */
        boolean findCellForSpan(int[] cellXY, int spanX, int spanY) {
            return findCellForSpan(cellXY, spanX, spanY, true);
        }

        boolean findCellForSpan(int[] cellXY, int spanX, int spanY, boolean clear) {
            final ArrayList<VacantCell> list = vacantCells;
            final int count = list.size();

            boolean found = false;

            if (this.spanX >= spanX && this.spanY >= spanY) {
                cellXY[0] = cellX;
                cellXY[1] = cellY;
                found = true;
            }

            // Look for an exact match first
            for (int i = 0; i < count; i++) {
                VacantCell cell = list.get(i);
                if (cell.spanX == spanX && cell.spanY == spanY) {
                    cellXY[0] = cell.cellX;
                    cellXY[1] = cell.cellY;
                    found = true;
                    break;
                }
            }

            // Look for the first cell large enough
            for (int i = 0; i < count; i++) {
                VacantCell cell = list.get(i);
                if (cell.spanX >= spanX && cell.spanY >= spanY) {
                    cellXY[0] = cell.cellX;
                    cellXY[1] = cell.cellY;
                    found = true;
                    break;
                }
            }

            if (clear)
                clearVacantCells();

            return found;
        }

        @Override
        public String toString() {
            return "Cell[view=" + (cell == null ? "null" : cell.getClass()) + ", x=" + cellX
                    + ", y=" + cellY + "]";
        }
    }

    public boolean lastDownOnOccupiedCell() {
        return mLastDownOnOccupiedCell;
    }

    public View findChildByCell(int cellX, int cellY) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.cellX == cellX && lp.cellY == cellY) {
                return child;
            }
        }
        return null;
    }

    public void moveChildInNormal(View child, final int[] targetXY) {
        final View view = child;
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        final int[] result = new int[2];
        cellToPoint(targetXY[0], targetXY[1], result);

        TranslateAnimation animation = new TranslateAnimation(0, result[0] - lp.x, 0, result[1]
                - lp.y);
        animation.setRepeatCount(0);
        animation.setDuration(300);
        animation.setInterpolator(new LinearInterpolator());
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation arg0) {
            }

            public void onAnimationRepeat(Animation arg0) {
            }

            public void onAnimationEnd(Animation arg0) {
                // update db
                ItemInfo info = (ItemInfo) view.getTag();
                info.cellX = targetXY[0];
                info.cellY = targetXY[1];
                LauncherModel.updateItemInDatabase(getContext(), info);
                onDropChild(view, targetXY);
                AnimManager.getInstance().startSingle(view);
            }
        });
        view.clearAnimation();
        view.startAnimation(animation);
    }

    public void rangeChilds(boolean anim) {
        if (!anim) {
            if (mIsInNavigate) {
                rangeInNavigate(false);
            } else {
                rangeInNormal(false);
            }
            return;
        }
        if (mIsChildAnim) {
            return;
        }
        mIsChildAnim = true;
        if (mIsInNavigate) {
            rangeInNavigate(true);
        } else {
            rangeInNormal(true);
        }
        Handler handler = getHandler();
        if (handler != null) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    correctOverlap();
                    mIsChildAnim = false;
                }
            }, 1000);
        }
    }

    public boolean isChildInAnim() {
        return mIsChildAnim;
    }

    public void correctOverlap() {
        final boolean portrait = mOrientationPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        boolean occupied[][] = new boolean[xCount][yCount];
        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                occupied[x][y] = false;
            }
        }
        final int count = getChildCount();
        final ArrayList<View> overLaps = new ArrayList<View>();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            for (int x = lp.cellX; x < lp.cellX + lp.cellHSpan && x < xCount; x++) {
                for (int y = lp.cellY; y < lp.cellY + lp.cellVSpan && y < yCount; y++) {
                    if (occupied[x][y]) {
                        overLaps.add(child);
                    } else {
                        occupied[x][y] = true;
                    }
                }
            }
        }
        final int N = overLaps.size();
        if (N <= 0) {
            return;
        }
        for (int i = 0; i < N; i++) {
            final View view = overLaps.get(i);
            final int[] vacant = new int[2];
            if (getVacantCell(vacant, 1, 1)) {
                ItemInfo info = (ItemInfo) view.getTag();
                info.cellX = vacant[0];
                info.cellY = vacant[1];
                LauncherModel.updateItemInDatabase(getContext(), info);
                if (mIsInNavigate) {
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    lp.cellX = vacant[0];
                    lp.cellY = vacant[1];
                } else {
                    onDropChild(view, vacant);
                }
            } else {
                // need to find a vacant cell
            }
        }
        if (mIsInNavigate) {
            int padding;
            if (mOrientationPortrait) {
                padding = (getWidth() - count * mCellWidth) / 2;
            } else {
                padding = (getHeight() - count * mCellHeight) / 2;
            }
            for (int i = 0; i < count; i++) {
                View view = getChildAt(i);
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                lp.navigate_padding = padding;
                view.requestLayout();
            }
        }
    }

    /**
     * @param cellX move start cell x
     * @param cellY move start cell y
     * @param isMoveToNext if move to next or previous
     * @return
     */
    public boolean movePos(int cellX, int cellY, boolean isMoveToNext) {
        if (mIsChildAnim) {
            return false;
        }
        mIsChildAnim = true;
        boolean result = false;
        if (mIsInNavigate) {
            result = movePosAtNavigate(cellX, cellY, isMoveToNext);
        } else {
            result = movePoslAtNormal(cellX, cellY, isMoveToNext);
        }
        if (result) {
            getHandler().postDelayed(new Runnable() {
                public void run() {
                    correctOverlap();
                    mIsChildAnim = false;
                }
            }, 1000);
        } else {
            mIsChildAnim = false;
        }
        return result;
    }

    public boolean movePoslAtNormal(int cellX, int cellY, boolean isMoveToNext) {
        int[] vacant = new int[2];
        if (!getVacantCell(vacant, 1, 1)) {
            return false;
        }
        final int N = getChildCount();
        if (isMoveToNext) {
            if (vacant[0] + vacant[1] * mShortAxisCells > cellX + cellY * mShortAxisCells) {
                for (int i = 0; i < N; i++) {
                    View child = getChildAt(i);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    if ((lp.cellX + lp.cellY * mShortAxisCells >= cellX + cellY * mShortAxisCells)
                            && (lp.cellX + lp.cellY * mShortAxisCells < vacant[0] + vacant[1]
                                    * mShortAxisCells)) {
                        final int cellTargetX = (lp.cellX + lp.cellY * 4 + 1) % 4;
                        final int cellTargetY = (lp.cellX + lp.cellY * 4 + 1) / 4;
                        Log.d(TAG, "movePoslAtNormal (" + lp.cellX + "," + lp.cellY + ") to ("
                                + cellTargetX + "," + cellTargetY + ")");
                        moveChildInNormal(child, new int[] {
                                cellTargetX, cellTargetY
                        });
                    }
                }
            } else {
                return false;
            }
        } else {
            if (vacant[0] + vacant[1] * mShortAxisCells < cellX + cellY * mShortAxisCells) {
                for (int i = 0; i < N; i++) {
                    View child = getChildAt(i);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    if ((lp.cellX + lp.cellY * mShortAxisCells <= cellX + cellY * mShortAxisCells)
                            && (lp.cellX + lp.cellY * mShortAxisCells > vacant[0] + vacant[1]
                                    * mShortAxisCells)) {
                        final int cellTargetX = (lp.cellX + lp.cellY * 4 - 1) % 4;
                        final int cellTargetY = (lp.cellX + lp.cellY * 4 - 1) / 4;
                        Log.d(TAG, "movePoslAtNormal (" + lp.cellX + "," + lp.cellY + ") to ("
                                + cellTargetX + "," + cellTargetY + ")");
                        moveChildInNormal(child, new int[] {
                                cellTargetX, cellTargetY
                        });
                    }
                }
            } else {
                return false;
            }
        }

        // final int N = getChildCount();
        // for (int i = 0; i < N; i++) {
        // View child = getChildAt(i);
        // LayoutParams lp = (LayoutParams) child.getLayoutParams();
        // if ((lp.cellX + lp.cellY * mShortAxisCells == cellX + cellY *
        // mShortAxisCells)
        // && (vacant[0] + vacant[1] * mShortAxisCells < cellX + cellY *
        // mShortAxisCells)) {
        // final int cellTargetX = (lp.cellX + lp.cellY * 4 - 1) % 4;
        // final int cellTargetY = (lp.cellX + lp.cellY * 4 - 1) / 4;
        // Log.d(TAG, "makeVacantCellAtNormal (" + lp.cellX + "," + lp.cellY +
        // ") to ("
        // + cellTargetX + "," + cellTargetY + ")");
        // moveChildInNormal(child, new int[] {
        // cellTargetX, cellTargetY
        // });
        // break;
        // } else if ((lp.cellX + lp.cellY * mShortAxisCells >= cellX + cellY *
        // mShortAxisCells)
        // && (vacant[0] + vacant[1] * mShortAxisCells > cellX + cellY *
        // mShortAxisCells)
        // && (lp.cellX + lp.cellY * mShortAxisCells < vacant[0] + vacant[1]
        // * mShortAxisCells)) {
        // final int cellTargetX = (lp.cellX + lp.cellY * 4 + 1) % 4;
        // final int cellTargetY = (lp.cellX + lp.cellY * 4 + 1) / 4;
        // Log.d(TAG, "makeVacantCellAtNormal (" + lp.cellX + "," + lp.cellY +
        // ") to ("
        // + cellTargetX + "," + cellTargetY + ")");
        // moveChildInNormal(child, new int[] {
        // cellTargetX, cellTargetY
        // });
        // }
        // }
        return true;
    }

    public boolean movePosAtNavigate(int cellX, int cellY, boolean isMoveToNext) {
        int[] vacant = new int[2];
        if (!getVacantCell(vacant, 1, 1)) {
            return false;
        }
        final int N = getChildCount();
        final int hSpaceLeft;
        if (mOrientationPortrait) {
            hSpaceLeft = (getWidth() - (mCellWidth * (N + 1) + mWidthGap * (N + 2))) / 2;
        } else {
            return false;
            //hSpaceLeft = (getHeight() - (mCellHeight * (N + 1) + mHeightGap * (N + 2))) / 2;
        }
        if (isMoveToNext) {
            for (int i = 0; i < N; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int[] cellTarget = new int[] {
                        lp.cellX, lp.cellY
                };
                if (lp.cellX >= cellX && lp.cellX < vacant[0] && vacant[0] > cellX) {
                    cellTarget[0] = (lp.cellX + lp.cellY * 4 + 1) % 4;
                    cellTarget[1] = (lp.cellX + lp.cellY * 4 + 1) / 4;
                }
                moveChildInNavigate(child, cellTarget, hSpaceLeft);
            }
        } else {
            if (vacant[0] < cellX) {
                for (int i = 0; i < N; i++) {
                    final View child = getChildAt(i);
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    final int[] cellTarget = new int[] {
                            lp.cellX, lp.cellY
                    };
                    if (lp.cellX <= cellX && lp.cellX > vacant[0]) {
                        cellTarget[0] = (lp.cellX + lp.cellY * 4 - 1) % 4;
                        cellTarget[1] = (lp.cellX + lp.cellY * 4 - 1) / 4;
                        moveChildInNavigate(child, cellTarget, hSpaceLeft);
                        break;
                    }
                }
            } else {
                return false;
            }
        }

        // if (cellX + cellY * mShortAxisCells > vacant[0] + vacant[1] *
        // mShortAxisCells) {
        // return false;
        // }
        // final int N = getChildCount();
        // final int hSpaceLeft = (getWidth() - (mCellWidth * (N + 1) +
        // mWidthGap * (N + 2))) / 2;
        // for (int i = 0; i < N; i++) {
        // final View child = getChildAt(i);
        // final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        // final int[] cellTarget = new int[] {
        // lp.cellX, lp.cellY
        // };
        // if (lp.cellX >= cellX && vacant[0] > cellX) {
        // cellTarget[0] = (lp.cellX + lp.cellY * 4 + 1) % 4;
        // cellTarget[1] = (lp.cellX + lp.cellY * 4 + 1) / 4;
        // } else if (lp.cellX == cellX && vacant[0] < cellX) {
        // cellTarget[0] = (lp.cellX + lp.cellY * 4 - 1) % 4;
        // cellTarget[1] = (lp.cellX + lp.cellY * 4 - 1) / 4;
        // }
        // Log.d(TAG, "makeVacantCellAtNavigate (" + lp.cellX + "," + lp.cellY +
        // ") to ("
        // + cellTarget[0] + "," + cellTarget[1] + ")");
        // moveChildInNavigate(child, cellTarget, hSpaceLeft);
        // }
        return true;
    }

    private void rangeInNormal(boolean anim) {
        int[] vacant = new int[2];
        if (!getVacantCell(vacant, 1, 1)) {
            return;
        }
        final int cellX = vacant[0];
        final int cellY = vacant[1];
        final int N = getChildCount();
        for (int i = 0; i < N; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.cellX + lp.cellY * mShortAxisCells > cellX + cellY * mShortAxisCells) {
                final int cellTargetX = (lp.cellX + lp.cellY * mShortAxisCells - 1) % 4;
                final int cellTargetY = (lp.cellX + lp.cellY * mShortAxisCells - 1) / 4;
                Log.d(TAG, "rangeInNormal (" + lp.cellX + "," + lp.cellY + ") to (" + cellTargetX
                        + "," + cellTargetY + ")");
                if (anim) {
                    moveChildInNormal(child, new int[] {
                            cellTargetX, cellTargetY
                    });
                } else {
                    ItemInfo info = (ItemInfo) child.getTag();
                    info.cellX = cellTargetX;
                    info.cellY = cellTargetY;
                    LauncherModel.updateItemInDatabase(getContext(), info);
                    onDropChild(child, new int[] {
                            cellTargetX, cellTargetY
                    });
                    AnimManager.getInstance().startSingle(child);
                }

            }
        }
    }

    private void rangeInNavigate(boolean anim) {
        final int[] vacant = new int[2];
        if (!getVacantCell(vacant, 1, 1)) {
            return;
        }
        final int N = getChildCount();
        final int padding;
        if (mOrientationPortrait) {
            padding = (getWidth() - (mCellWidth * N + mWidthGap * (N + 1))) / 2;
        } else {
            padding = (getHeight() - (mCellHeight * N + mHeightGap * (N + 1))) / 2;
        }
        for (int i = 0; i < N; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int targetXY[] = new int[] {
                    lp.cellX, lp.cellY
            };
            if (mOrientationPortrait) {
                if (lp.cellX > vacant[0]) {
                    targetXY[0] = (lp.cellX + lp.cellY * 4 - 1) % 4;
                    targetXY[1] = (lp.cellX + lp.cellY * 4 - 1) / 4;
                }
            } else {
                if (lp.cellY > vacant[1]) {
                    targetXY[0] = 0;
                    targetXY[1] = lp.cellY - 1;
                }
            }

            // Log.d(TAG, "rangeInNavigate (" + lp.cellX + "," + lp.cellY +
            // ") to (" + targetXY[0]
            // + "," + targetXY[1] + ")");
            if (anim) {
                moveChildInNavigate(child, targetXY, padding);
            } else {
                ItemInfo info = (ItemInfo) child.getTag();
                info.cellX = targetXY[0];
                info.cellY = targetXY[1];
                LauncherModel.updateItemInDatabase(getContext(), info);
                lp.navigate_padding = padding;
                onDropChild(child, targetXY);
                AnimManager.getInstance().startSingle(child);
            }
        }
    }

    private void moveChildInNavigate(final View child, final int[] targetXY, final int paddingLeft) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int oldPos;
        final int newPos;
        final TranslateAnimation animation;
        if (mOrientationPortrait) {
            oldPos = lp.x;
            newPos = paddingLeft + targetXY[0] * mCellWidth;
            animation = new TranslateAnimation(0, newPos - oldPos, 0, 0);
        } else {
            oldPos = lp.y;
            newPos = paddingLeft + targetXY[1] * mCellHeight;
            animation = new TranslateAnimation(0, 0, 0, newPos - oldPos);
        }
        Log.d(TAG, "moveChildInNavigate " + oldPos + "," + newPos);
        animation.setRepeatCount(0);
        animation.setDuration(300);
        animation.setInterpolator(new LinearInterpolator());
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation arg0) {
            }

            public void onAnimationRepeat(Animation arg0) {
            }

            public void onAnimationEnd(Animation arg0) {
                ItemInfo info = (ItemInfo) child.getTag();
                info.cellX = targetXY[0];
                info.cellY = targetXY[1];
                LauncherModel.updateItemInDatabase(getContext(), info);
                lp.navigate_padding = paddingLeft;
                onDropChild(child, targetXY);
                AnimManager.getInstance().startSingle(child);
            }
        });
        child.clearAnimation();
        child.startAnimation(animation);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return true;
    }

}
