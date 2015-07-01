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

import com.android.iphonelauncher.CellLayout.LayoutParams;
import com.android.ui.IphoneShortcutCallback;
import com.android.util.AnimManager;

import android.R.integer;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

/**
 * TextView that draws a bubble behind the text. We cannot use a
 * LineBackgroundSpan because we want to make the bubble taller than the text
 * and TextView's clip is too aggressive.
 */
public class BubbleTextView extends TextView implements DropTarget, IphoneShortcutCallback {

    private static final String TAG = "BubbleTextView";

    private Launcher mLauncher;

    Drawable mOriIcon;

    Drawable mGridFolderIcon;

    boolean hasReflectionEffect = false;

    Bitmap mReflectionBitmap;

    Bitmap mMaskBitmap;

    Rect mOverLeftRect;

    Rect mOverRightrRect;

    Rect mCenterRect;

    boolean mPressed;

    private int mReflectionTopMargin = 0;

    private boolean mIsFolderIconMode = false;

    boolean mAleardyFolderIcon = false;

    public BubbleTextView(Context context) {
        super(context);
        init();
    }

    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setFocusable(true);
        if (DisplayMetrics.DENSITY_DEVICE <= 120) {
            setTextSize(16.f);
        }
        setDrawingCacheEnabled(true);
        mOverLeftRect = new Rect();
        mOverRightrRect = new Rect();
        mCenterRect = new Rect();
        final int cellWidth = getResources().getDimensionPixelSize(R.dimen.workspace_cell_width);
        final int cellHeight = getResources().getDimensionPixelSize(R.dimen.workspace_cell_height);
        final int dragOverRectW = getResources().getDimensionPixelSize(
                R.dimen.iphone_icon_dragover_rect_w);
        final int dragOverRectH = getResources().getDimensionPixelSize(
                R.dimen.iphone_icon_dragover_rect_h);
        // final int appSize = (int)
        // getResources().getDimensionPixelSize(R.dimen.iphone_mask_size);
        // final int right = (cellWidth - appSize) / 2 + appSize / 3;
        // final int top = getPaddingTop() + appSize / 2;
        mOverLeftRect.set(0, cellHeight - dragOverRectH, dragOverRectW, cellHeight);
        mOverRightrRect.set(cellWidth - dragOverRectW, cellHeight - dragOverRectH, cellWidth,
                cellHeight);
        mCenterRect.set(dragOverRectW, 0, cellWidth - dragOverRectW, cellHeight);
        mReflectionTopMargin = getResources().getDimensionPixelSize(
                R.dimen.iphone_reflection_topmargin);
    }

    @Override
    public void draw(Canvas canvas) {

        super.draw(canvas);

        if (hasReflectionEffect) {
            if (mReflectionBitmap != null) {
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                final Rect rect = new Rect();
                final int left = getCompoundPaddingLeft();
                final int top = getExtendedPaddingTop() - mReflectionTopMargin;
                final int offset = (getWidth() - mReflectionBitmap.getWidth()) / 2;
                rect.set(left + offset, top, left + offset + mReflectionBitmap.getWidth(),
                        getHeight());
                final Rect src = new Rect(0, 0, rect.width(), rect.height());
                canvas.translate(scrollX, scrollY);
                canvas.drawBitmap(mReflectionBitmap, src, rect, null);
                canvas.translate(-scrollX, -scrollY);
            }
        }
        if (mPressed) {
            if (mMaskBitmap == null) {
                FastBitmapDrawable drawable = (FastBitmapDrawable) getCompoundDrawables()[1];
                if (drawable != null) {
                    Bitmap bm = drawable.getBitmap();
                    Bitmap alpha = bm.extractAlpha();
                    mMaskBitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(),
                            Config.ARGB_8888);
                    Canvas c = new Canvas(mMaskBitmap);
                    Paint paint = new Paint();
                    // BlurMaskFilter blur = new BlurMaskFilter(8,
                    // BlurMaskFilter.Blur.SOLID);
                    paint.setColor(0x000000);
                    paint.setAlpha(0x7F);
                    paint.setXfermode(null);
                    // paint.setMaskFilter(blur);
                    paint.setDither(true);
                    c.drawBitmap(alpha, 0, 0, paint);
                }
            }
            if (mMaskBitmap != null) {
                Rect outRect = new Rect();
                getDrawingRect(outRect);
                int top = getPaddingTop();
                int left = getPaddingLeft() + (getWidth() - mMaskBitmap.getWidth()) / 2;
                canvas.drawBitmap(mMaskBitmap, outRect.left + left, outRect.top + top, null);
            }
        }

    }

    public void setIphonePressed(boolean isPress) {
        mPressed = isPress;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        ViewParent parent = getParent();
        if (parent == null) {
            return;
        }
        Workspace targetWorkspace = (Workspace) parent.getParent();
        final int workspaceType = targetWorkspace.getType();
        int container = 0;
        switch (workspaceType) {
            case Workspace.WORKSPACE_NAVIGATEBAR:
                container = LauncherSettings.Favorites.CONTAINER_NAVIGATEBAR;
                break;
            case Workspace.WORKSPACE_WORKSPACE:
                container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                break;
            default:
                return;
        }

        // get layout params
        final LayoutParams currentLp = (LayoutParams) getLayoutParams();

        // remove my in parent
        removeSelfInParent(mLauncher.getDragController());

        // make the folder info.

        final UserFolderInfo folderInfo = new UserFolderInfo();
        folderInfo.title = getResources().getText(R.string.folder_name);
        // add folder into db
        boolean result = LauncherModel.addItemToDatabase(getContext(), folderInfo, container,
                targetWorkspace.getCurrentScreen(), currentLp.cellX, currentLp.cellY, true);
        
        //android.util.Log.w(Launcher.TAG, "onDrop()==result:"+result);

        // add into new folder
        // CellLayout.CellInfo cellInfo = mSourceWorkspace.getCurrentDragInfo();

        final ShortcutInfo thisTag = (ShortcutInfo) getTag();
        final ShortcutInfo sourceTag = (ShortcutInfo) dragInfo;
        final int oldSourceScreen = sourceTag.screen;
        folderInfo.add(thisTag);
        folderInfo.add(sourceTag);

        // add folder items into db
        LauncherModel.addOrMoveItemInDatabase(mLauncher, thisTag, folderInfo.id, 0, 0, 0);
        LauncherModel.addOrMoveItemInDatabase(mLauncher, sourceTag, folderInfo.id, 0, 0, 0);

        // Create the view
        final FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, mLauncher,
                (ViewGroup) targetWorkspace.getChildAt(targetWorkspace.getCurrentScreen()),
                folderInfo);

        // add into target cell
        targetWorkspace
                .addInCurrentScreen(newFolder, currentLp.cellX, currentLp.cellY, 1, 1, false);
        // jz fix for add new folder, rotate screen, the folder will show gone.
        //mLauncher.addItemToDesktop(folderInfo);
        
        // start anim
        AnimManager.getInstance().startSingle(newFolder);

        // range source cell
        Workspace sourceWorkspace = (Workspace) source;
        CellLayout sourceCellLayout = (CellLayout) sourceWorkspace.getChildAt(oldSourceScreen);
        sourceCellLayout.rangeChilds(false);

        // open folder
        targetWorkspace.getHandler().postDelayed(new Runnable() {
            public void run() {
                mLauncher.handleIphoneFolderClick(folderInfo, newFolder);
            }
        }, 200);
    }

    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }

    public boolean onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {

        // if (!mOverLeftRect.contains(x, y)) {
        // return false;
        // }
        CellLayout targetCelllayout = (CellLayout) getParent();
        if (targetCelllayout == null) {
            return false;
        }
        LayoutParams targetLp = (LayoutParams) getLayoutParams();
        final int tempTargetCellX = targetLp.cellX;
        final int tempTargetCellY = targetLp.cellY;

        boolean result = false;

        boolean needToShowAsFolderIcon = false;

        if (mOverLeftRect.contains(x, y)) {
            result = targetCelllayout.movePos(tempTargetCellX, tempTargetCellY, true);
        } else if (mOverRightrRect.contains(x, y)) {
            result = targetCelllayout.movePos(tempTargetCellX, tempTargetCellY, false);
        } else if (!targetCelllayout.isNavigate() && mCenterRect.contains(x, y)) {
            needToShowAsFolderIcon = true;
        }

        if (!mAleardyFolderIcon) {
            if (needToShowAsFolderIcon) {
                setFolderIcon(dragInfo);
            } else {
                setOrilIcon(dragInfo);
            }
        }

        return result;
        // if (!targetCelllayout.movePos(tempTargetCellX, tempTargetCellY,
        // false)) {
        // return false;
        // }
        // Object tag = getTag();
        // if (tag instanceof ShortcutInfo) {
        // if (mOriIcon == null) {
        // final ShortcutInfo info = (ShortcutInfo) tag;
        // final Bitmap mFolderIcon = info.getIcon(mLauncher.getIconCache());
        // setIphoneIcon(mFolderIcon);
        // } else {
        // setCompoundDrawablesWithIntrinsicBounds(null, mOriIcon, null, null);
        // }
        // }
        // return true;
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        setOrilIcon(dragInfo);
    }

    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {

        ViewParent parent = getParent();
        if (parent == null) {
            return false;
        }

        if (!mCenterRect.contains(x, y) && !mAleardyFolderIcon) {
            return false;
        }

        if (dragInfo instanceof UserFolderInfo) {
            // it is a foler icon return
            return false;
        }
        final ItemInfo myInfo = (ItemInfo) getTag();
        if (myInfo.container >= 0) {
            // i am in a folder return
            return false;
        }
        
        if(parent instanceof CellLayout && ((CellLayout)parent).isNavigate()){
        	return false;
        }
        
        // if (mOverRect.contains(x, y)) {
        // return false;
        // }
        final ItemInfo item = (ItemInfo) dragInfo;
        final int itemType = item.itemType;
        return (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT);
    }

    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, Rect recycle) {
        return null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if (getAnimation() != null) {
                // ItemInfo info = (ItemInfo) getTag();
                // if (info.container > 0) {
                // // this view is in a folder.
                // Folder folder = mLauncher.getOpenIphoneFolder();
                // if (folder != null) {
                // folder.startDrag(this);
                // return true;
                // }
                // } else {
                // CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) ((View)
                // getParent())
                // .getTag();
                // if (cellInfo == null) {
                // Log.d(TAG, "cellinfo is null");
                // return true;
                // }
                // final View parent = (View) getParent().getParent();
                // if (parent instanceof Workspace) {
                // ((Workspace) parent).startDrag(cellInfo);
                // return true;
                // }
                // }
                // }
                setIphonePressed(true);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setIphonePressed(false);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * iphone shortcut callback
     */
    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public void setReflectionEffect(boolean has) {
        hasReflectionEffect = has
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (hasReflectionEffect) {
            final Drawable drawable = getCompoundDrawables()[1];
            Bitmap bitmap = ((FastBitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                mReflectionBitmap = Utilities.createReflectionImage(bitmap);
            }
        } else {
            if (mReflectionBitmap != null) {
                mReflectionBitmap.recycle();
                mReflectionBitmap = null;
            }
        }
        postInvalidate();
    }

    public void setIphoneIcon(Bitmap icon) {
        final Drawable drawable = new FastBitmapDrawable(icon);
        setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        mOriIcon = drawable;
    }

    public void removeSelfInParent(DragController dragController) {
        clearAnimation();
        AnimManager.getInstance().removeControllers(this);
        dragController.removeDropTarget((DropTarget) this);
        ((CellLayout) getParent()).removeView(this);
    }

    private void setFolderIcon(Object dragInfo) {
        if (mIsFolderIconMode) {
            return;
        }
        if (dragInfo instanceof UserFolderInfo) {
            // it is a foler icon return
            return;
        }
        final ItemInfo myInfo = (ItemInfo) getTag();
        if (myInfo.container >= 0) {
            // i am in a folder return
            return;
        }
        if (mGridFolderIcon == null) {
            final ShortcutInfo info = (ShortcutInfo) getTag();
            final Bitmap mFolderIcon = info.getIcon(mLauncher.getIconCache());
            final Bitmap gridFolderIcon = Utilities.makeGridFolderIcons(new Bitmap[] {
                mFolderIcon
            }, getContext());
            mGridFolderIcon = new FastBitmapDrawable(gridFolderIcon);
        }
        if (hasReflectionEffect) {
            mReflectionBitmap = Utilities
                    .createReflectionImage(((FastBitmapDrawable) mGridFolderIcon).getBitmap());
        }
        setCompoundDrawablesWithIntrinsicBounds(null, mGridFolderIcon, null, null);
        mIsFolderIconMode = true;
    }

    private void setOrilIcon(Object dragInfo) {

        if (!mIsFolderIconMode) {
            return;
        }

        if (dragInfo instanceof UserFolderInfo) {
            // it is a foler icon return
            return;
        }
        final ItemInfo myInfo = (ItemInfo) getTag();
        if (myInfo.container >= 0) {
            // i am in a folder return
            return;
        }
        if (mOriIcon == null) {
            final ShortcutInfo info = (ShortcutInfo) getTag();
            final Bitmap mFolderIcon = info.getIcon(mLauncher.getIconCache());
            mOriIcon = new FastBitmapDrawable(mFolderIcon);
        }
        if (hasReflectionEffect) {
            mReflectionBitmap = Utilities.createReflectionImage(((FastBitmapDrawable) mOriIcon)
                    .getBitmap());
        }
        setCompoundDrawablesWithIntrinsicBounds(null, mOriIcon, null, null);

        mIsFolderIconMode = false;
    }

}
