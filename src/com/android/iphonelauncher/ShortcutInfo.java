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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Represents a launchable icon on the workspaces and in folders.
 */
public class ShortcutInfo extends AppShortcutInfo {

    /**
     * The application name.
     */
    //CharSequence title;

    /**
     * The intent used to start the application.
     */
    //Intent intent;

    /**
     * Indicates whether the icon comes from an application's resource (if
     * false) or from a custom Bitmap (if true.)
     */
    boolean customIcon;

    /**
     * Indicates whether we're using the default fallback icon instead of
     * something from the app.
     */
    boolean usingFallbackIcon;

    /**
     * Indicates whether the shortcut is on external storage and may go away at
     * any time.
     */
    boolean onExternalStorage;

    /**
     * If isShortcut=true and customIcon=false, this contains a reference to the
     * shortcut icon as an application's resource.
     */
    Intent.ShortcutIconResource iconResource;

    /**
     * The application icon.
     */
    //private Bitmap mIcon;

    ShortcutInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }

    public ShortcutInfo(ShortcutInfo info) {
        super(info);
//        title = info.title.toString();
//        intent = new Intent(info.intent);
        if (info.iconResource != null) {
            iconResource = new Intent.ShortcutIconResource();
            iconResource.packageName = info.iconResource.packageName;
            iconResource.resourceName = info.iconResource.resourceName;
        }
        //mIcon = info.mIcon; // TODO: should make a copy here. maybe we don't
                            // need this ctor at all
        customIcon = info.customIcon;
    }

    /** TODO: Remove this. It's only called by ApplicationInfo.makeShortcut. */
    public ShortcutInfo(ApplicationInfo info) {
        super(info);
//        title = info.title.toString();
//        intent = new Intent(info.intent);
        // customIcon = true;
        // mIcon = info.iconBitmap;
        // itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }
    
    public ShortcutInfo(PackageManager pm, ResolveInfo info, IconCache iconCache) {
    	super(pm, info, iconCache);
    }
    
    public ShortcutInfo(PackageManager pm, ResolveInfo info, ComponentName componentName, IconCache iconCache) {
    	super(pm, info, componentName, iconCache, null);
    }
    
//    @Override
//    public void updateInfo(ItemInfo info){
//    	super.updateInfo(info);
//    	
//    	if(info instanceof ApplicationInfo){
//    		
//    		intent = new Intent(((ApplicationInfo)info).intent);
//    		title = ((ApplicationInfo)info).title.toString();
//    		
//    	} else if(info instanceof ShortcutInfo){
//    		
//    		intent = new Intent(((ShortcutInfo)info).intent);
//    		title = ((ShortcutInfo)info).title.toString();
//    		
//    	}
//    }
    
//    @Override
//    public boolean equals(ApplicationInfo info){
//    	if(info != null)
//    		return intent.getComponent().equals(info.componentName);
//    	return super.equals(info);
//    }
//    
//    @Override
//    public boolean equals(ShortcutInfo info){
//    	if(info != null)
//    		return intent.getComponent().equals(info.intent.getComponent());
//    	return super.equals(info);
//    }
    
//    public boolean isEqualComponent(ShortcutInfo info){
//    	return info.intent.getComponent().equals(intent.getComponent());
//    }

//    public void setIcon(Bitmap b) {
//        mIcon = b;
//    }
//
//    public Bitmap getIcon(IconCache iconCache) {
//        if (mIcon == null) {
//            mIcon = iconCache.getIcon(this.intent);
//        }
//        return mIcon;
//    }

    /**
     * Creates the application intent based on a component name and various
     * launch flags. Sets {@link #itemType} to
     * {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     * 
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
//    final void setActivity(ComponentName className, int launchFlags) {
//    	String action = Intent.ACTION_MAIN;
//    	if(className != null && "com.android.contacts".equals(className.getPackageName())){
//            if("com.android.contacts.DialtactsActivity".equals(className.getClassName())){
//            	action = Intent.ACTION_DIAL;
//            }
//        }
//        intent = new Intent(action);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        intent.setComponent(className);
//        intent.setFlags(launchFlags);
//        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
//    }

    @Override
    void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);

        String titleStr = title != null ? title.toString() : null;
        values.put(LauncherSettings.BaseLauncherColumns.TITLE, titleStr);

        String uri = intent != null ? intent.toUri(0) : null;
        values.put(LauncherSettings.BaseLauncherColumns.INTENT, uri);

        if (customIcon) {
            values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE,
                    LauncherSettings.BaseLauncherColumns.ICON_TYPE_BITMAP);
            // writeBitmap(values, mIcon);
        } else {
            if (onExternalStorage && !usingFallbackIcon) {
                writeBitmap(values, mIcon);
            }
            values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE,
                    LauncherSettings.BaseLauncherColumns.ICON_TYPE_RESOURCE);
            if (iconResource != null) {
                values.put(LauncherSettings.BaseLauncherColumns.ICON_PACKAGE,
                        iconResource.packageName);
                values.put(LauncherSettings.BaseLauncherColumns.ICON_RESOURCE,
                        iconResource.resourceName);
            }
        }
    }

    @Override
    public String toString() {
        return "ShortcutInfo("+ super.toString() +")";
    }

    @Override
    void unbind() {
        super.unbind();
    }

    public static void dumpShortcutInfoList(String tag, String label, ArrayList<ShortcutInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (ShortcutInfo info : list) {
            Log.d(tag, "   title=\"" + info.title + " icon=" + info.mIcon + " customIcon="
                    + info.customIcon);
        }
    }

    public ApplicationInfo makeApplicationInfo() {
        return new ApplicationInfo(this);
    }
}
