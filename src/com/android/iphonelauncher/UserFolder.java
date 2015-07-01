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

package com.android.iphonelauncher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import com.android.iphonelauncher.R;

/**
 * Folder which contains applications or shortcuts chosen by the user.
 */
public class UserFolder extends Folder {
    private static final String TAG = "Launcher.UserFolder";

    public UserFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Creates a new UserFolder, inflated from R.layout.user_folder.
     * 
     * @param context The application's context.
     * @return A new UserFolder.
     */
    static UserFolder fromXml(Context context) {
        return (UserFolder) LayoutInflater.from(context).inflate(R.layout.user_folder, null);
    }

    // public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
    // int yOffset,
    // DragView dragView, Object dragInfo) {
    // final ItemInfo item = (ItemInfo) dragInfo;
    // final int itemType = item.itemType;
    // return (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
    // itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
    // && item.container != mInfo.id;
    // }
    //
    // public Rect estimateDropLocation(DragSource source, int x, int y, int
    // xOffset, int yOffset,
    // DragView dragView, Object dragInfo, Rect recycle) {
    // return null;
    // }
    //
    // public void onDrop(DragSource source, int x, int y, int xOffset, int
    // yOffset,
    // DragView dragView, Object dragInfo) {
    // ShortcutInfo item;
    // if (dragInfo instanceof ApplicationInfo) {
    // // Came from all apps -- make a copy
    // item = ((ApplicationInfo) dragInfo).makeShortcut();
    // } else {
    // item = (ShortcutInfo) dragInfo;
    // }
    // final ArrayList<ShortcutInfo> infos = ((UserFolderInfo) mInfo).contents;
    // infos.add(item);
    // // initCell(infos);
    // addItem(item);
    // // ((ShortcutsAdapter) mContent.getAdapter()).add(item);
    // // LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0,
    // // 0, 0);
    // }
    //
    // public void onDragEnter(DragSource source, int x, int y, int xOffset, int
    // yOffset,
    // DragView dragView, Object dragInfo) {
    // }
    //
    // public boolean onDragOver(DragSource source, int x, int y, int xOffset,
    // int yOffset,
    // DragView dragView, Object dragInfo) {
    // return false;
    // }
    //
    // public void onDragExit(DragSource source, int x, int y, int xOffset, int
    // yOffset,
    // DragView dragView, Object dragInfo) {
    // }
    //
    // @Override
    // public void onDropCompleted(View target, boolean success) {
    // if (success) {
    // // ShortcutsAdapter adapter = (ShortcutsAdapter)
    // // mContent.getAdapter();
    // // adapter.remove(mDragItem);
    //
    // } else {
    // final ArrayList<ShortcutInfo> infos = ((UserFolderInfo) mInfo).contents;
    // infos.add(mDragItem);
    // // initCell(infos);
    // addItem(mDragItem);
    // }
    // }

    void bind(FolderInfo info, View folderIcon) {
        super.bind(info, folderIcon);
        // setContentAdapter(new ShortcutsAdapter(mContext, ((UserFolderInfo)
        // info).contents));
    }

    // When the folder opens, we need to refresh the GridView's selection by
    // forcing a layout
    @Override
    void onOpen() {
        super.onOpen();
        requestFocus();
    }
}
