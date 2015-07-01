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

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Represents an app in AllAppsView.
 */
public class AppShortcutInfo extends ItemInfo {

    /**
     * The application name.
     */
    CharSequence title;

    /**
     * The intent used to start the application.
     */
    Intent intent;
    
    ComponentName componentName;

    /**
     * The application icon.
     */
    /*protected */Bitmap mIcon;
    
    public static final int DOWNLOADED_FLAG = 1;
    public static final int UPDATED_SYSTEM_APP_FLAG = 2;

    public int flags = 0;

    AppShortcutInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }

    
    public AppShortcutInfo(AppShortcutInfo info) {
        super(info);
        
        if(info != null){
	        mIcon = info.mIcon;
	        flags = info.flags;
	        
	        title = info.title.toString();
	        intent = new Intent(info.intent);
	        
	        if(info.componentName != null)
	        	componentName = info.componentName;
	        else if(intent != null)
	        	componentName = intent.getComponent();
        }
    }
    
    public AppShortcutInfo(PackageManager pm, ResolveInfo info) {
    	this(pm, info, null);
    }
    
    public AppShortcutInfo(PackageManager pm, ResolveInfo info, IconCache iconCache) {
    	
    	this(pm, info, iconCache, null);
    }
    
    public AppShortcutInfo(PackageManager pm, ResolveInfo info, IconCache iconCache, AppShortcutInfo item) {
    	
    	this(pm, info, new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name), iconCache, item);
    }

    public AppShortcutInfo(PackageManager pm, ResolveInfo info, ComponentName componentName, IconCache iconCache, AppShortcutInfo item) {
    	this(item);

        reSetFlag(pm, info);
        
        if(item == null){
        	
//        	updateInfo(item);
//        	
//        } else {
        	
        	this.componentName = componentName;
	        this.container = ItemInfo.NO_ID;
	        this.setActivity(componentName, Intent.FLAG_ACTIVITY_NEW_TASK
	                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        
        if(iconCache != null)
        	iconCache.getTitleAndIcon(this, info);

//        if(intent == null)
//        	Log.e(Launcher.TAG, "AppShortcutInfo()===intent is null==");
    }
    
    @Override
    public void updateInfo(ItemInfo info){
    	super.updateInfo(info);
    	
    	if(info instanceof AppShortcutInfo){
    		
    		intent = new Intent(((AppShortcutInfo)info).intent);
    		title = ((AppShortcutInfo)info).title.toString();
    		
    	} 
    }
    
    @Override
    public boolean equals(AppShortcutInfo info){
    	
    	if(componentName != null)
    		return componentName.equals(info.componentName);
    	else if(intent != null)
    		return intent.getComponent().equals(info.intent.getComponent());
    	
    	return super.equals(info);
    }
    
    public boolean equals(ComponentName component){
    	if(componentName != null)
    		return componentName.equals(component);
    	else if(intent != null)
    		return intent.getComponent().equals(component);
    	
    	return false;
    }

    public boolean equals(String packageName, String className){
    	if(componentName != null){
    		return (componentName.getPackageName().equals(packageName) 
    				&& componentName.getClassName().equals(className));
    	}else if(intent != null){
    		ComponentName cn = intent.getComponent();
    		return (cn.getPackageName().equals(packageName) 
    				&& cn.getClassName().equals(className));
    	}
    	
    	return false;
    }
    
    public ComponentName getComponent(){
    	if(componentName != null){
    		return componentName;
    	}else if(intent != null){
    		return intent.getComponent();
    	}
    	return null;
    }
    
    public String getPackageName(){
    	if(componentName != null){
    		return componentName.getPackageName();
    	}else if(intent != null){
    		return intent.getComponent().getPackageName();
    	}
    	return "";
    }
    
    public String getClassName(){
    	if(componentName != null){
    		return componentName.getClassName();
    	}else if(intent != null){
    		return intent.getComponent().getClassName();
    	}
    	return "";
    }
    
    public void reSetFlag(PackageManager pm, ResolveInfo info){
    	try {
            int appFlags = pm.getApplicationInfo(info.activityInfo.applicationInfo.packageName, 0).flags;
            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                flags |= DOWNLOADED_FLAG;

                if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    flags |= UPDATED_SYSTEM_APP_FLAG;
                }
            }
            //firstInstallTime = pm.getPackageInfo(packageName, 0).firstInstallTime;
        } catch (NameNotFoundException e) {
            //QsLog.LogE("PackageManager.getApplicationInfo failed for " + packageName);
        }
    }
    
    public boolean isInstalledApp(){
    	return (flags > 0);
    }

    public void setIcon(Bitmap b) {
    	mIcon = b;
    }

    public Bitmap getIcon(IconCache iconCache) {
        if (mIcon == null) {
        	mIcon = iconCache.getIcon(this.intent);
        }
        return mIcon;
    }
    /**
     * Creates the application intent based on a component name and various
     * launch flags. Sets {@link #itemType} to
     * {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     * 
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
    	String action = Intent.ACTION_MAIN;
    	if(className != null && "com.android.contacts".equals(className.getPackageName())){
            if("com.android.contacts.DialtactsActivity".equals(className.getClassName())){
            	action = Intent.ACTION_DIAL;
            }
        }
        intent = new Intent(action);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }
    
//    @Override
//    void onAddToDatabase(ContentValues values) {
//        super.onAddToDatabase(values);
//
//        String titleStr = title != null ? title.toString() : null;
//        values.put(LauncherSettings.BaseLauncherColumns.TITLE, titleStr);
//
//        String uri = intent != null ? intent.toUri(0) : null;
//        values.put(LauncherSettings.BaseLauncherColumns.INTENT, uri);
//    }

    @Override
    public String toString() {
        return super.toString() + "title=" + title.toString() /* + "=intent="+intent.toString()*/;
    }

    public static void dumpApplicationInfoList(String tag, String label,
            ArrayList<AppShortcutInfo> list) {
        Log.d(tag, label + " size=" + list.size());
//        for (AppShortcutInfo info : list) {
//            Log.d(tag, "   title=\"" + info.title + "\" titleBitmap=" + info.titleBitmap
//                    + " iconBitmap=" + info.mIcon);
//        }
    }

//    public ShortcutInfo makeShortcut() {
//        return new ShortcutInfo(this);
//    }
}
