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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * An icon that can appear on in the workspace representing an
 * {@link UserFolder}.
 */
public class FolderIcon extends BubbleTextView {

    public static final int MAX_ITEMS = 12;

    private static final int MAX_GRID_ICONS = 9;

    private UserFolderInfo mInfo;

    private Launcher mLauncher;
    
    private final int mMaxItemsCount;

    // private Drawable mCloseIcon;
    //
    // private Drawable mOpenIcon;

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAleardyFolderIcon = true;
        final int nMaxColumn = context.getResources().getInteger(R.integer.config_cellLayoutColumnCount);
        final int nRows = context.getResources().getInteger(R.integer.config_cellLayoutRowsCount);
        mMaxItemsCount = nMaxColumn * (nRows - 1);
    }

    public FolderIcon(Context context) {
        this(context, null);
        //mAleardyFolderIcon = true;
    }

    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            UserFolderInfo folderInfo) {

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, null, false);
        // final Resources resources = launcher.getResources();
        // Drawable d = resources.getDrawable(R.drawable.ic_launcher_folder);
        // icon.mCloseIcon = d;
        // icon.mOpenIcon =
        // resources.getDrawable(R.drawable.ic_launcher_folder_open);
        icon.setCompoundDrawablesWithIntrinsicBounds(null,
                makeFolderDrawable(folderInfo, launcher), null, null);
        icon.setText(folderInfo.title);
        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;

        return icon;
    }

    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // final ItemInfo item = (ItemInfo) dragInfo;
        // final int itemType = item.itemType;
        // return (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
        // || itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
        // && item.container != mInfo.id;
        if (mInfo.size() >= mMaxItemsCount) {
            // can only put MAX_ITEMS items
            return false;
        }
        return super.acceptDrop(source, x, y, xOffset, yOffset, dragView, dragInfo);
    }

    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, Rect recycle) {
        return null;
    }

    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        ShortcutInfo item;
        if (dragInfo instanceof ApplicationInfo) {
            // Came from all apps -- make a copy
            item = ((ApplicationInfo) dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo) dragInfo;
        }
        final int oldSourceScreen = item.screen;
        mInfo.add(item);
//Log.w(Launcher.TAG, "FolderIcon::onDrop()=====container:"+item.container);
        setCompoundDrawablesWithIntrinsicBounds(null, makeFolderDrawable(mInfo, mLauncher), null,
                null);
        LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0, 0, 0);
        // need range
        Workspace sourceWorkspace = (Workspace) source;
        CellLayout sourceCellLayout = (CellLayout) sourceWorkspace.getChildAt(oldSourceScreen);
        sourceCellLayout.rangeChilds(false);
        
        // open folder
        ((View)getParent().getParent()).getHandler().postDelayed(new Runnable() {
            public void run() {
                mLauncher.handleIphoneFolderClick(mInfo, FolderIcon.this);
            }
        }, 200);
 
    }

    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // setCompoundDrawablesWithIntrinsicBounds(null, mOpenIcon, null, null);
    }

    public boolean onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        return super.onDragOver(source, x, y, xOffset, yOffset, dragView, dragInfo);
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null,
        // null);
    }

 
    static Drawable makeFolderDrawable(UserFolderInfo info, Launcher launcher) {
        final int N = info.contents.size();
        final int NUM = N > MAX_GRID_ICONS ? MAX_GRID_ICONS : N;
        final Bitmap[] bitmaps = new Bitmap[NUM];
        for (int i = 0; i < NUM; i++) {
            bitmaps[i] = ((AppShortcutInfo)info.get(i)).getIcon(launcher.getIconCache());
        }

        final Drawable drawable = new FastBitmapDrawable(Utilities.makeGridFolderIcons(bitmaps,
                launcher));
        return drawable;
    }

    public void resetDrawableIcon() {
        setCompoundDrawablesWithIntrinsicBounds(null, makeFolderDrawable(mInfo, mLauncher), null,
                null);
    }
}
