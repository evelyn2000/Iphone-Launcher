/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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

import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.android.internal.util.XmlUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;



/**
 * Stores the list of all applications for the all apps view.
 */
class AllAppsList {
	static final String TAG = "Launcher2.AllAppsList";
    static final boolean DEBUG_LOADERS_REORDER = false;
    public static final int DEFAULT_APPLICATIONS_NUMBER = 42;
    //public static final int DEFAULT_FOLDERS_NUMBER = 5;
    
    private static final String TAG_TOPPACKAGES = "toppackages";
    private static final String TAG_TOPPACKAGE = "TopPackage";
    
    /** The list off all apps. */
    public ArrayList<ItemInfo> data =
            new ArrayList<ItemInfo>(DEFAULT_APPLICATIONS_NUMBER);
    
    //public HashMap<Long, FolderInfo> mFolders = new HashMap<Long, FolderInfo>(DEFAULT_FOLDERS_NUMBER);
    
    /** The list of apps that have been added since the last notify() call. */
    public ArrayList<ItemInfo> added =
            new ArrayList<ItemInfo>(DEFAULT_APPLICATIONS_NUMBER);
    /** The list of apps that have been removed since the last notify() call. */
    public ArrayList<ItemInfo> removed = new ArrayList<ItemInfo>();
    /** The list of apps that have been modified since the last notify() call. */
    public ArrayList<ItemInfo> modified = new ArrayList<ItemInfo>();

    static final String STK_PACKAGE = "com.android.stk";
    static final String STK2_PACKAGE = "com.android.stk2";
    
    private IconCache mIconCache;
    
    static ArrayList<TopPackage> mTopPackages;
    
    static class TopPackage {
    	public TopPackage (String packagename,String classname,int order) {
    		mPackageName = packagename;
    		mClassName = classname;
    		mOrder = order;
    		mIndex = -1;
    		
    	}
    	
    	String mPackageName;
    	String mClassName;
    	int mOrder;
    	
    	int mIndex;
    }
    
    static ArrayList<IngorePackage> mIngorePackages;
    private static final String TAG_INGNOREPACKAGES = "IgnorePackage";
    static class IngorePackage{
        public IngorePackage (String packagename,String classname) {
            mPackageName = packagename;
            mClassName = classname;
        }
        String mPackageName;
        String mClassName;
    }

    /**
     * Boring constructor.
     */
    public AllAppsList(IconCache iconCache) {
        mIconCache = iconCache;
    }

    /**
     * Add the supplied ApplicationInfo objects to the list, and enqueue it into the
     * list to broadcast when notify() is called.
     *
     * If the app is already in the list, doesn't add it.
     */
//    public void addApplication(ApplicationInfo info) {
//        data.add(info);
//        added.add(info);
//    }
    
    public void add(AppShortcutInfo info){
    	if (findActivity(data, info.getComponent())) {
            return;
        }
    	//Log.d(TAG, "add()=="+info.toString());
    	data.add(info);
        added.add(info);
    }
    
    public void addFolder(ItemInfo info){
//    	if (findActivity(data, info.componentName)) {
//            return;
//        }
    	
    	data.add(info);
        //added.add(info);
    }
    
    public void addItem(ItemInfo info){
    	data.add(info);
    }
    
    public ArrayList<ItemInfo> getAllData(){
    	return data;
    }
//    
//    public void addFolder(long id, FolderInfo info){
//    	mFolders.put(id, info);
//    }
	   
    public void clear() {
        data.clear();
        //mFolders.clear();
        // TODO: do we clear these too?
        added.clear();
        removed.clear();
        modified.clear();
    }

    public int size() {
        return data.size();
    }

    public ItemInfo get(int index) {
        return data.get(index);
    }

    /**
     * Add the icons for the supplied apk called packageName.
     */    
    public void addPackage(PackageManager packageManager, Context context, String packageName) {
        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
        if(DEBUG_LOADERS_REORDER){
        	Log.d(TAG, "addPackage()==packageName:"+packageName+"==size:"+matches.size());
        }
        if (matches.size() > 0) {
            for (ResolveInfo info : matches) {
                //add(new ApplicationInfo(packageManager, info, mIconCache));
            	if(!isIngoreApplist(info.activityInfo.applicationInfo.packageName, info.activityInfo.name)){
            		add(new ShortcutInfo(packageManager, info, mIconCache));
            	}
            }
        }
        //reorderApplist();
    }

    /**
     * Remove the apps for the given apk identified by packageName.
     */
    public void removePackage(String packageName) {
        final List<ItemInfo> data = this.data;
        if(DEBUG_LOADERS_REORDER){
        	Log.d(TAG, "removePackage()==packageName:"+packageName+"==size:"+data.size());
        }
        
        for (int i = data.size() - 1; i >= 0; i--) {
        	ItemInfo item = data.get(i);
        	if(item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
        			|| item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
        		AppShortcutInfo info = (AppShortcutInfo)item;
	            //final ComponentName component = info.getComponent();
	            if (packageName.equals(info.getPackageName())) {
	                removed.add(info);
	                data.remove(i);
	            }
        	}/* else if(item.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER){
        		
        		if(removeFolderApplication((UserFolderInfo)item, packageName, false) == 0){
            		removed.add(item);
            		data.remove(i);
            	}
        	}*/
        }
        // This is more aggressive than it needs to be.
        mIconCache.flush();
    }

    /**
     * Add and remove icons for this package which has been updated.
     */
    public void updatePackage(PackageManager packageManager, Context context, String packageName) {
        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
        if(DEBUG_LOADERS_REORDER){
        	Log.d(TAG, "updatePackage()==packageName:"+packageName+"==size:"+matches.size());
        }
        if (matches.size() > 0) {
            // Find disabled/removed activities and remove them from data and add them
            // to the removed list.
            for (int i = data.size() - 1; i >= 0; i--) {
                final ItemInfo info = data.get(i);
                if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
                		|| info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
                	AppShortcutInfo appInfo = (AppShortcutInfo)info;
	                final ComponentName component = appInfo.getComponent();
	                if (packageName.equals(component.getPackageName())) {
	                    if (!findActivity(matches, component)) {
	                        removed.add(appInfo);
	                        mIconCache.remove(component);
	                        data.remove(i);
	                    }
	                }
	                
                } /*else if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER){
                	
                	UserFolderInfo folderInfo = (UserFolderInfo)info;
                	for(int j=folderInfo.size() - 1; j >= 0; j--){
                		AppShortcutInfo appInfo = (AppShortcutInfo)folderInfo.get(j);
                		ComponentName cn = appInfo.intent.getComponent();
            			if (packageName.equals(cn.getPackageName())) {
            				if (!findActivity(matches, cn)) {
    	                        removed.add(appInfo);
    	                        mIconCache.remove(cn);
    	                        folderInfo.remove(j);
    	                    }
                        }
            		}
                	
                	if(folderInfo.size() == 0){
                		removed.add(info);
                		data.remove(i);
                	}
                }*/
            }

            // Find enabled activities and add them to the adapter
            // Also updates existing activities with new labels/icons
            int count = matches.size();
            for (int i = 0; i < count; i++) {
                final ResolveInfo info = matches.get(i);
                AppShortcutInfo applicationInfo = findApplicationInfoLocked(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name);
                if (applicationInfo == null) {
                	
                	if(!isIngoreApplist(info.activityInfo.applicationInfo.packageName, info.activityInfo.name)){
                		add(new ShortcutInfo(packageManager, info, mIconCache));
                	}
                } else {
                    mIconCache.remove(applicationInfo.getComponent());
                    mIconCache.getTitleAndIcon(applicationInfo, info);
                    modified.add(applicationInfo);
                }
            }
        } else {
            // findActivitiesForPackage cannot get disabled Activity.
            // a simple process for STK Test
            if(packageName.compareTo(STK_PACKAGE) == 0 || packageName.compareTo(STK2_PACKAGE) == 0) {
                removeDisabledStkActivity(packageName);
            }
        }
    }

    private void removeDisabledStkActivity(String packageName) {
        for (int i=data.size()-1; i>=0; i--) {
        	ItemInfo item = data.get(i);
        	if(item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
        			|| item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
        		final AppShortcutInfo appInfo = (AppShortcutInfo)item;
	            //final ApplicationInfo applicationInfo = data.get(i);
	            final ComponentName component = appInfo.getComponent();
	            if (packageName.equals(component.getPackageName())) {
	                removed.add(appInfo);
	                mIconCache.remove(component);
	                data.remove(i);
	            }
        	} /*else if(item.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER){
        		
        		if(removeFolderApplication((UserFolderInfo)item, packageName, true) == 0){
            		removed.add(item);
            		data.remove(i);
            	}
            }*/
        }
    }
    
    private int removeFolderApplication(UserFolderInfo folderInfo, String packageName, boolean rmIcon){
    	for(int j=folderInfo.size() - 1; j >= 0; j--){
    		AppShortcutInfo appInfo = (AppShortcutInfo)folderInfo.get(j);
    		ComponentName cn = appInfo.getComponent();
			if (packageName.equals(cn.getPackageName())) {
                removed.add(appInfo);
                if(rmIcon)
                	mIconCache.remove(cn);
                folderInfo.remove(j);
            }
		}
    	
    	return folderInfo.size();
    }

    /**
     * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
     */
    private static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        final List<ResolveInfo> matches = new ArrayList<ResolveInfo>();

        if (apps != null) {
            // Find all activities that match the packageName
            int count = apps.size();
            for (int i = 0; i < count; i++) {
                final ResolveInfo info = apps.get(i);
                final ActivityInfo activityInfo = info.activityInfo;
                if (packageName.equals(activityInfo.packageName)) {
                    matches.add(info);
                }
            }
        }

        return matches;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(List<ResolveInfo> apps, ComponentName component) {
        final String className = component.getClassName();
        for (ResolveInfo info : apps) {
            final ActivityInfo activityInfo = info.activityInfo;
            if (activityInfo.name.equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */    
    private static boolean findActivity(ArrayList<ItemInfo> apps, ComponentName component) {
        final int N = apps.size();
        for (int i=0; i<N; i++) {
        	final ItemInfo info = apps.get(i);
        	
        	if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
        			|| info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
        		
	            if (((AppShortcutInfo)info).equals(component)) {
	                return true;
	            }
        	}/*else if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER){
        		
        		if(((UserFolderInfo)info).get(component) >= 0)
        			return true;
        	}*/
        }
        return false;
    }
    
    private static AppShortcutInfo getActivity(ArrayList<ItemInfo> apps, ComponentName component) {
        final int N = apps.size();
        for (int i=0; i<N; i++) {
        	final ItemInfo info = apps.get(i);
        	if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
        			|| info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
	            if (((AppShortcutInfo)info).equals(component)) {
	                return (AppShortcutInfo)info;
	            }
        	}/*else if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER){
        		int index = ((UserFolderInfo)info).get(component);
        		if(index >= 0)
        			return (AppShortcutInfo)((UserFolderInfo)info).get(index);
        	}*/
        }
        return null;
    }

    /**
     * Find an ApplicationInfo object for the given packageName and className.
     */
    private AppShortcutInfo findApplicationInfoLocked(String packageName, String className) {
        for (ItemInfo item: data) {
        	if(item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
        			|| item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
        		AppShortcutInfo info = (AppShortcutInfo)item;
                if (info.equals(packageName, className)) {
                    return info;
                }
        	} /*else if(item.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER){
        		UserFolderInfo folderInfo = (UserFolderInfo)item;
        		int index = folderInfo.get(packageName, className);
        		if(index >= 0)
        			return (AppShortcutInfo)folderInfo.get(index);

        	}*/
        }
        return null;
    }
    
    
    /**
     * Loads the default set of default to packages from an xml file.
     *
     * @param context The context 
     */
    static boolean loadTopPackage(Context context) {
    	boolean bRet = false;
    	
    	if (mTopPackages == null) {
    		mTopPackages = new ArrayList<TopPackage>();
    	} else {
    		return true;
    	}

        try {
            XmlResourceParser parser = context.getResources().getXml(R.xml.default_toppackage);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            XmlUtils.beginDocument(parser, TAG_TOPPACKAGES);

            final int depth = parser.getDepth();

            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth) 
            		&& type != XmlPullParser.END_DOCUMENT) {

                if (type != XmlPullParser.START_TAG) {
                    continue;
                }                    

                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TopPackage);                    
                
                mTopPackages.add(new TopPackage(a.getString(R.styleable.TopPackage_topPackageName),
                		a.getString(R.styleable.TopPackage_topClassName),
                		a.getInt(R.styleable.TopPackage_topOrder, 0)));
                
                Log.d(TAG, "loadTopPackage packageName==" + a.getString(R.styleable.TopPackage_topPackageName)); 
                Log.d(TAG, "loadTopPackage className==" + a.getString(R.styleable.TopPackage_topClassName));

                a.recycle();
            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Got exception parsing toppackage.", e);
        } catch (IOException e) {
            Log.w(TAG, "Got exception parsing toppackage.", e);
        }

        return bRet;
    }   
    
    static int getTopPackageIndex(ApplicationInfo appInfo) {
    	int retIndex = -1;
        if (mTopPackages == null || mTopPackages.isEmpty() || appInfo == null) {
        	return retIndex;
        } 
        
        for (TopPackage tp : mTopPackages) {        	                
    		if (appInfo.componentName.getPackageName().equals(tp.mPackageName) 
    				&& appInfo.componentName.getClassName().equals(tp.mClassName)) {

    			retIndex = tp.mOrder;    			
    			break;
    		}               	                
        } 
        
        return retIndex;        
    }
    
    void dumpData() {
        int loop2 = 0;
    	for (ItemInfo ai : data) {
    		if (DEBUG_LOADERS_REORDER) {
    			Log.d(TAG, "reorderApplist data loop2==" + loop2);
    			Log.d(TAG, "reorderApplist data ==" + ai.toString()); 
    		}
    		loop2++;
    	} 	
    }
    
    boolean isIngoreApplist(String packageName, String className){
    	
    	if (mIngorePackages != null && !mIngorePackages.isEmpty()) {
    		for(IngorePackage ip : mIngorePackages){
    			if(ip.mPackageName.equals(packageName) && ip.mClassName.equals(className))
    				return true;
    		}
        }
    	
    	return false;
    }
    
    void removeIngoreApplist(){
    	if (mIngorePackages == null||mIngorePackages.isEmpty()) {
        	return ;
        }
    	
    	for(IngorePackage ip : mIngorePackages)
        {
        	for(int i=data.size()-1; i>=0; i--){
        		ItemInfo info = data.get(i);
        		if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
        				|| info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
        			AppShortcutInfo ai = (AppShortcutInfo)info;
        			if (DEBUG_LOADERS_REORDER) {
                        Log.d(TAG, "reorderApplist ignore packageName==" + ai.componentName); 
                    }
        			
	        		if (ai.equals(ip.mPackageName, ip.mClassName)) {
	                    
	                    data.remove(i);
	                    added.remove(ai);
	                    break;
	                }
        		}
        	}
        }
    }
    
    class SortByItemCellInfo  implements Comparator<ItemInfo>{
		public int compare(ItemInfo s1, ItemInfo s2) {
//			ItemInfo s1 = (ItemInfo) o1;
//			ItemInfo s2 = (ItemInfo) o2;
			//if (s1.getAge() > s2.getAge())
			if(s1.id == ItemInfo.NO_ID)
				return 1;
			if(s2.id == ItemInfo.NO_ID)
				return -1;
			
			if(s1.screen > s2.screen)
				return 1;
			if(s1.cellY > s2.cellY)
				return 1;
			if(s1.cellX > s2.cellX)
				return 1;
			if(s1.container > s2.container)
				return 1;
			
			return -1;
		}
    }
    
    void reorderApplist() {
    	
    	if(true){
    		Collections.sort(data, new SortByItemCellInfo());
    	} else {
    	
	        final long sortTime = DEBUG_LOADERS_REORDER ? SystemClock.uptimeMillis() : 0;                                
	
	        if(mIngorePackages != null && !mIngorePackages.isEmpty()){
		        for(IngorePackage ip : mIngorePackages)
		        {
		        	for(int i=data.size()-1; i>=0; i--){
		        		ItemInfo info = data.get(i);
		        		if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
		        				|| info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
		        			AppShortcutInfo ai = (AppShortcutInfo)info;
		        		
			        		if (ai.equals(ip.mPackageName, ip.mClassName)) {
			                    if (DEBUG_LOADERS_REORDER) {
			                        Log.d(TAG, "reorderApplist ignore packageName==" + ai.componentName.getPackageName()); 
			                    }
			                    data.remove(i);
			                    added.remove(ai);
			                    break;
			                }
		        		}
		        	}
		        }
	        }
	        
	        if (mTopPackages == null || mTopPackages.isEmpty()) {
	        	return ;
	        }
	        
	        ArrayList<AppShortcutInfo> dataReorder =
	            new ArrayList<AppShortcutInfo>(DEFAULT_APPLICATIONS_NUMBER); 
	        
	        for (TopPackage tp : mTopPackages) { 
	        	int loop = 0;
	        	int newIndex = 0;
	        	for (ItemInfo item : added) {
	        		if(item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
	        				|| item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
	        			AppShortcutInfo ai = (AppShortcutInfo)item;
		        		if (DEBUG_LOADERS_REORDER) {
		        			Log.d(TAG, "reorderApplist remove loop==" + loop);
		        			Log.d(TAG, "reorderApplist remove packageName==" + ai.componentName.getPackageName()); 
		        		}
		                
		        		if (ai.equals(tp.mPackageName, tp.mClassName)) {
		            		if (DEBUG_LOADERS_REORDER) {
		            			Log.d(TAG, "reorderApplist remove newIndex==" + newIndex); 
		            		}
		            		
		            		data.remove(ai);
		            		dataReorder.add(ai);	
		
		//        			dumpData();
		        			
		        			break;
		        		}
	        		}
	        		loop++;
	        	}                	                
	        }  
	        
	        for (TopPackage tp : mTopPackages) { 
	        	int loop = 0;
	        	int newIndex = 0;
	        	for (AppShortcutInfo ai : dataReorder) {
	        		if (DEBUG_LOADERS_REORDER) {
	        			Log.d(TAG, "reorderApplist added loop==" + loop);
	        			Log.d(TAG, "reorderApplist added packageName==" + ai.componentName.getPackageName()); 
	        		}
	                
	        		if (ai.equals(tp.mPackageName, tp.mClassName)) {
	        			newIndex = Math.min(Math.max(tp.mOrder, 0), added.size());
	            		if (DEBUG_LOADERS_REORDER) {
	            			Log.d(TAG, "reorderApplist added newIndex==" + newIndex); 
	            		}
	
	            		data.add(newIndex,ai);	
	
	//        			dumpData();
	        			
	        			break;
	        		}
	        		loop++;
	        	}                	                
	        } 
	        
	        if (added.size() == data.size()) {
	        	//added = (ArrayList<ItemInfo>) data.clone();	
	        	Log.d(TAG, "reorderApplist added.size() == data.size() : "+ data.size());
	        }
	//        
	//        if (DEBUG_LOADERS_REORDER) {
	//            Log.d(TAG, "sort and reorder took "
	//                    + (SystemClock.uptimeMillis()-sortTime) + "ms");
	//        }        
    	}
    }
    
    static boolean loadIngorePackage(Context context) {
        boolean bRet = false;
        
        if (mIngorePackages == null) {
            mIngorePackages = new ArrayList<IngorePackage>();
        } else {
            return true;
        }

        try {
            XmlResourceParser parser = context.getResources().getXml(R.xml.ignore_toppackage);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            XmlUtils.beginDocument(parser, TAG_INGNOREPACKAGES);

            final int depth = parser.getDepth();

            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth) 
                    && type != XmlPullParser.END_DOCUMENT) {

                if (type != XmlPullParser.START_TAG) {
                    continue;
                }                    

                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IgnorePackage);                    
                
                mIngorePackages.add(new IngorePackage(a.getString(R.styleable.IgnorePackage_ignorePackageName),
                        a.getString(R.styleable.IgnorePackage_ignoreClassName)));
                if(DEBUG_LOADERS_REORDER){
	                Log.d(TAG, "loadIgnorePackage packageName==" + a.getString(R.styleable.IgnorePackage_ignorePackageName)); 
	                Log.d(TAG, "loadIgnorePackage className==" + a.getString(R.styleable.IgnorePackage_ignoreClassName));
                }
                a.recycle();
            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Got exception parsing IgnorePackage.", e);
        } catch (IOException e) {
            Log.w(TAG, "Got exception parsing IgnorePackage.", e);
        }

        return bRet;
    }   
}
