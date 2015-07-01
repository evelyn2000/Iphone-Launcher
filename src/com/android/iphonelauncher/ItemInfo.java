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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Represents an item in the launcher.
 */
class ItemInfo {
    
    static final int NO_ID = -1;
    
    /**
     * The id in the settings database for this item
     */
    long id = NO_ID;
    
    /**
     * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_USER_FOLDER}, or
     * {@link LauncherSettings.Favorites#ITEM_TYPE_APPWIDGET}.
     */
    int itemType;
    
    /**
     * The id of the container that holds this item. For the desktop, this will be 
     * {@link LauncherSettings.Favorites#CONTAINER_DESKTOP}. For the all applications folder it
     * will be {@link #NO_ID} (since it is not stored in the settings DB). For user folders
     * it will be the id of the folder.
     */
    long container = NO_ID;
    
    /**
     * Iindicates the screen in which the shortcut appears.
     */
    int screen = -1;
    
    /**
     * Indicates the X position of the associated cell.
     */
    int cellX = -1;

    /**
     * Indicates the Y position of the associated cell.
     */
    int cellY = -1;

    /**
     * Indicates the X cell span.
     */
    int spanX = 1;

    /**
     * Indicates the Y cell span.
     */
    int spanY = 1;

    /**
     * Indicates whether the item is a gesture.
     */
    boolean isGesture = false;
    
    //int appOrderId = -1;
    
    

    ItemInfo() {
    }

    ItemInfo(ItemInfo info) {
    	
    	if(info != null){
	        id = info.id;
	        cellX = info.cellX;
	        cellY = info.cellY;
	        spanX = info.spanX;
	        spanY = info.spanY;
	        screen = info.screen;
	        itemType = info.itemType;
	        container = info.container;
    	}
		//appOrderId = info.appOrderId;
		
		
    }
    
    void updateInfo(ItemInfo info){
    	id = info.id;
        cellX = info.cellX;
        cellY = info.cellY;
        spanX = info.spanX;
        spanY = info.spanY;
        screen = info.screen;
        itemType = info.itemType;
        container = info.container;
		//appOrderId = info.appOrderId;
    }
    //@Override
    public boolean equals(ItemInfo info){
    	if(info != null ){
    		
    		if(info instanceof AppShortcutInfo)
    			return equals((AppShortcutInfo)info);
    		
//    		if(info instanceof ApplicationInfo)
//    			return equals((ApplicationInfo)info);
//    		
//    		if(info instanceof ShortcutInfo)
//    			return equals((ShortcutInfo)info);
    		
    		if(info.cellX == cellX && cellY == info.cellY && screen == info.screen 
    			&& itemType == info.itemType && container == info.container){
    			return true;
    		}
    	}
    	return false;
    }
    
//    public boolean equals(ApplicationInfo info){
////    	if(info != null)
////    		return intent.getComponent().equals(info.componentName);
//    	return false;
//    }
//    
//    public boolean equals(ShortcutInfo info){
////    	if(info != null)
////    		return intent.getComponent().equals(info.intent.getComponent());
//    	return false;
//    }
    
    public boolean equals(AppShortcutInfo info){
    	return false;
    }

    /**
     * Write the fields of this item to the DB
     * 
     * @param values
     */
    void onAddToDatabase(ContentValues values) { 
        values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, itemType);
        if (!isGesture) {
            values.put(LauncherSettings.Favorites.CONTAINER, container);
            values.put(LauncherSettings.Favorites.SCREEN, screen);
            values.put(LauncherSettings.Favorites.CELLX, cellX);
            values.put(LauncherSettings.Favorites.CELLY, cellY);
            values.put(LauncherSettings.Favorites.SPANX, spanX);
            values.put(LauncherSettings.Favorites.SPANY, spanY);
            //values.put(LauncherSettings.Favorites.APPORDER_ID, appOrderId);
            //values.put(LauncherSettings.Favorites.APP_FLAGS, flags);
        }
    }

    static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

    static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = flattenBitmap(bitmap);
            values.put(LauncherSettings.Favorites.ICON, data);
        }
    }
    
    void unbind() {
    }

    @Override
    public String toString() {
        return "==(id=" + this.id + " screen=" + this.screen +" X="+cellX+" Y="+cellY
        +"  type=" + (this.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ? 
        		"app" : (this.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT ? 
        		"shortcut" : (this.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER ? "folder" : "unkown"))) 
        + " container="+(this.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ? 
        		"desk" : (this.container == LauncherSettings.Favorites.CONTAINER_NAVIGATEBAR ? 
        		"bar" : (this.container == NO_ID ? "NO_ID" : this.container)))+")";
    }
    
    public boolean isInFolder(){
    	return (container > 0 ? true : false);
    }
}
