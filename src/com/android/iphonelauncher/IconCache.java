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

import com.android.util.Common;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.HashMap;

/**
 * Cache of application icons. Icons can be made from any thread.
 */
public class IconCache {
    private static final String TAG = "Launcher2.IconCache";

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

    private static class CacheEntry {
        public Bitmap icon;

        public String title;

        public Bitmap titleBitmap;
    }
    
    /*public final int mDefaultIconList[] = new int[]{
    		R.drawable.zzzz_iphone_settings,
    		R.drawable.zzzz_iphone_browser,
    		R.drawable.zzzz_iphone_sms,
    		R.drawable.zzzz_iphone_call,
    		R.drawable.zzzz_iphone_contacts,
    		R.drawable.zzzz_iphone_camera,
    		R.drawable.zzzz_iphone_soundrecorder,
    		R.drawable.zzzz_iphone_filebrowser,
    		R.drawable.zzzz_iphone_fm,
    		R.drawable.zzzz_iphone_mail,
    		R.drawable.zzzz_iphone_caculator,
    		//R.drawable.zzzz_iphone_calendar,
    		R.drawable.zzzz_iphone_music,
    		R.drawable.zzzz_iphone_clock,
    		R.drawable.zzzz_iphone_photoes,
    		R.drawable.zzzz_iphone_weather,
    		R.drawable.zzzz_iphone_google_search,
    		R.drawable.zzzz_iphone_voicedialer,
    		
    		R.drawable.zzzz_iphone_google_talk,
    		R.drawable.zzzz_iphone_gmail,
    		R.drawable.zzzz_iphone_market,
    		R.drawable.zzzz_iphone_maps,
    		R.drawable.zzzz_iphone_voicesearch,
    		
    		R.drawable.zzzz_iphone_fruitninja,
    		R.drawable.zzzz_iphone_angrybird,
    		
    		R.drawable.zzzz_iphone_msn,
    		R.drawable.zzzz_iphone_qq,
    		R.drawable.zzzz_iphone_fetion,
    		
    		R.drawable.zzzz_iphone_compass,
    		R.drawable.zzzz_iphone_ibooks,
    		
    		
    		//R.drawable.zzzz_iphone_app2sd,
    		//R.drawable.zzzz_iphone_appstore,
    		//R.drawable.zzzz_iphone_sim_tool,
    		
    		//R.drawable.zzzz_iphone_economic,
    		//R.drawable.zzzz_iphone_electorch,
    		
    };
    
    public final String mDefaultIconNameList[] = new String[]{
    		"com.android.settings.Settings",
    		"com.android.browser.BrowserActivity",
    		"com.android.mms.ui.ConversationList",
    		"com.android.contacts.DialtactsActivity",
    		"com.android.contacts.DialtactsContactsEntryActivity",
    		"com.mediatek.camera.Camera",
    		"com.android.soundrecorder.SoundRecorder",
    		"com.qs.android.fileex.ExplorerActivity",
    		"com.mediatek.FMRadio.FMRadioActivity",
    		"com.android.email.activity.Welcome",
    		"com.android.calculator2.Calculator",
    		//"com.android.calendar.LaunchActivity",
    		"com.android.music.MusicBrowserActivity",
    		"com.android.deskclock.DeskClock",
    		"com.cooliris.media.Gallery", //"com.android.camera.GalleryPicker",
    		"com.qishang.android.widget.MainActivity",
    		"com.android.quicksearchbox.SearchActivity", // search
    		"com.android.voicedialer.VoiceDialerActivity",
    		
    		"com.google.android.talk.SigningInActivity",
    		"com.google.android.gm.ConversationListActivityGmail",
    		"com.android.vending.AssetBrowserActivity",
    		"com.google.android.maps.MapsActivity", // gmap
    		"com.google.android.apps.googlevoice.SplashActivity", // 
    		
    		"com.halfbrick.fruitninja.FruitNinjaActivity", // fruitninja
    		"com.rovio.ka3d.App",
    		
    		"com.xrath.jmsnx.MsnMain", 
    		"com.tencent.qq.SplashActivity",
    		"cn.com.fetion.android.activities.StartActivit",
    		"com.apksoftware.compass.Compass",
    };//com.android.music.MusicBrowserActivity
*/
    private final Bitmap mDefaultIcon;

    private final LauncherApplication mContext;

    private final PackageManager mPackageManager;

    private final Utilities.BubbleText mBubble;

    private final HashMap<ComponentName, CacheEntry> mCache = new HashMap<ComponentName, CacheEntry>(
            INITIAL_ICON_CACHE_CAPACITY);

    public IconCache(LauncherApplication context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mBubble = new Utilities.BubbleText(context);
        mDefaultIcon = makeDefaultIcon();
    }

    private Bitmap makeDefaultIcon() {
        Drawable d = mPackageManager.getDefaultActivityIcon();
        int w = d.getIntrinsicWidth();
        int h = d.getIntrinsicHeight();
        Bitmap b = Bitmap.createBitmap(Math.max(w, 1), Math.max(h, 1), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        return b;
    }
    
    /*public int getDefaultIconRes(String componentName)
    {
    	int nLength = mDefaultIconNameList.length;
    	for(int i=0; i<nLength; i++)
    	{
    		if(mDefaultIconNameList[i].equals(componentName))
    			return mDefaultIconList[i];
    	}
    	
    	return 0;
    }*/
    
    private int getDefaultIconRes(String cls)
    {
        if(cls.equals("com.android.settings.Settings")){
            return R.drawable.zzzz_iphone_settings;
        }else if(cls.equals("com.android.browser.BrowserActivity") 
        		|| cls.equals("com.boatgo.browser.BrowserActivity")){
            return R.drawable.zzzz_iphone_browser;
        }else if(cls.equals("com.android.mms.ui.ConversationList") 
                || cls.equals("com.android.mms.ui.BootActivity")){
            return R.drawable.zzzz_iphone_sms;
        }else if(cls.equals("com.android.contacts.DialtactsActivity")
                ||cls.equals("com.android.contacts.activities.DialtactsActivity")){
            return R.drawable.zzzz_iphone_call;
        }else if(cls.equals("com.android.contacts.DialtactsContactsEntryActivity") 
            || cls.equals("com.android.contacts.activities.PeopleActivity")
            || cls.equals("com.android.contacts.QsContactsListActivity")){
            return R.drawable.zzzz_iphone_contacts;
        }else if(cls.equals("com.mediatek.camera.Camera")
                || cls.equals("com.android.camera.Camera")){
            return R.drawable.zzzz_iphone_camera;
        }else if(cls.equals("com.android.soundrecorder.SoundRecorder"))
            return R.drawable.zzzz_iphone_soundrecorder;
        else if(cls.equals("com.qs.android.fileex.ExplorerActivity") 
        	|| cls.equals("com.mediatek.filemanager.FileManagerOperationActivity")){
            return R.drawable.zzzz_iphone_filemanager;
        }else if(cls.equals("com.mediatek.FMRadio.FMRadioActivity"))
            return R.drawable.zzzz_iphone_fm;
        else if(cls.equals("com.android.email.activity.Welcome"))
            return R.drawable.zzzz_iphone_mail;
        else if(cls.equals("com.android.calculator2.Calculator"))
            return R.drawable.zzzz_iphone_caculator;
        else if(cls.equals("com.android.music.MusicBrowserActivity"))
            return R.drawable.zzzz_iphone_music;
        else if(cls.equals("com.android.deskclock.DeskClock") 
        		|| cls.equals("com.aedesign.deskclock.DeskClockGroupActivity")){
            return R.drawable.zzzz_iphone_clock;
        } else if(cls.equals("com.cooliris.media.Gallery") 
        		|| cls.equals("com.android.camera.GalleryPicker")
        		|| cls.equals("com.silencecork.photography.activity.AlbumActivity")
        		|| cls.equals("com.android.gallery3d.app.Gallery")){
            return R.drawable.zzzz_iphone_photoes;
        } else if(cls.equals("com.qishang.android.widget.MainActivity")
        		|| cls.equals("com.aedesign.iphoneweather.activity.LaunchActivity")
        		|| cls.equals("com.youba.WeatherForecast.WeatherForecastActivity")
        		|| cls.equals("com.iphonestyle.weather.activity.MainActivity")){
            return R.drawable.zzzz_iphone_weather;
        }else if(cls.equals("com.android.quicksearchbox.SearchActivity"))
            return R.drawable.zzzz_iphone_google_search;
        else if(cls.equals("com.android.voicedialer.VoiceDialerActivity"))
            return R.drawable.zzzz_iphone_voicedialer;
        else if(cls.equals("com.google.android.talk.SigningInActivity"))
            return R.drawable.zzzz_iphone_google_talk;
        else if(cls.equals("com.google.android.gm.ConversationListActivityGmail"))
            return R.drawable.zzzz_iphone_gmail;
        else if(cls.equals("com.android.vending.AssetBrowserActivity") 
        	|| cls.equals("com.uucun105294.android.cms.activity.MarketLoginAndRegisterActivity")
        	|| cls.equals("com.hiapk.marketpho.MarketMainFrame")){
            return R.drawable.zzzz_iphone_appstore;
        }else if(cls.equals("com.google.android.maps.MapsActivity")
        		|| cls.equals("com.baidu.BaiduMap.BaiduMap"))
            return R.drawable.zzzz_iphone_maps;
        else if(cls.equals("com.google.android.apps.googlevoice.SplashActivity"))
            return R.drawable.zzzz_iphone_voicesearch;
        else if(cls.equals("com.halfbrick.fruitninja.FruitNinjaActivity"))
            return R.drawable.zzzz_iphone_fruitninja;
        else if(cls.equals("com.rovio.ka3d.App"))
            return R.drawable.zzzz_iphone_angrybird;
        else if(cls.equals("com.xrath.jmsnx.MsnMain"))
            return R.drawable.zzzz_iphone_msn;
        else if(cls.equals("com.tencent.qq.SplashActivity"))
            return R.drawable.zzzz_iphone_qq;
        else if(cls.equals("cn.com.fetion.android.activities.StartActivit"))
            return R.drawable.zzzz_iphone_fetion;
        else if(cls.equals("com.apksoftware.compass.Compass"))
            return R.drawable.zzzz_iphone_compass;
        else if(cls.equals("com.bluelotus.cncal.MainActivity")
        		||cls.equals("com.android.calendar.LaunchActivity")
        		||cls.equals("com.android.calendar.AllInOneActivity")){
            return R.drawable.zzzz_iphone_calendar;
        // 2012 01 04
        }else if(cls.equals("com.mediatek.app.mtv.ChannelListActivity"))
            return R.drawable.zzzz_iphone_atv;
        else if(cls.equals("com.mediatek.bluetooth.prx.monitor.PrxmDeviceMgmtActivity"))
            return R.drawable.zzzz_iphone_bt_prxm_launcher;
        else if(cls.equals("com.mediatek.app.touchpanel.Calibrator"))
            return R.drawable.zzzz_iphone_calibrator;
        else if(cls.equals("com.android.contacts.DialtactsCallLogEntryActivity"))
            return R.drawable.zzzz_iphone_contacts_calllog;
        else if(cls.equals("com.android.providers.downloads.ui.DownloadList"))
            return R.drawable.zzzz_iphone_download;
        else if(cls.equals("com.mediatek.filemanager.FileManagerActivity"))
            return R.drawable.zzzz_iphone_filemanager;
        else if(cls.equals("com.android.qishang.QsLedFlashlight.FlashlightActivity"))
            return R.drawable.zzzz_iphone_flashlight;
        else if(cls.equals("com.mediatek.wifip2pwizardy.WifiP2PWizardy"))
            return R.drawable.zzzz_iphone_wifi_icon;
        else if(cls.equals("com.mediatek.bluetooth.hid.BluetoothHidActivity"))
            return R.drawable.zzzz_iphone_wireless_kb;
        else if(cls.equals("com.android.qworldclock.QWorldClock"))
            return R.drawable.zzzz_iphone_worldclock;
        else if(cls.equals("com.chaozh.iReader.ui.activity.WelcomeActivity"))
        	return R.drawable.zzzz_iphone_ireader;
        else if(cls.equals("com.android.music.QS_VideoList")
        		|| cls.equals("com.mediatek.videoplayer.MovieListActivity")){
        	return R.drawable.zzzz_iphone_video;
        } else if(cls.equals("com.nlucas.notificationtoaster.NotificationToasterPreferences")){
        	return R.drawable.zzzz_iphone_notificationtoaster;
        } else if(cls.equals("com.mediatek.todos.TodosActivity")){
        	return R.drawable.zzzz_iphone_memo;
        } else if(cls.equals("com.mediatek.backuprestore.MainActivity")){
        	return R.drawable.zzzz_iphone_backup_restore;
        } else if(cls.equals("com.android.settings.Settings$WifiSettingsActivity")){
        	return R.drawable.zzzz_iphone_wlan_settings;
        } else if(cls.equals("com.mediatek.notebook.NotesList")){
        	return R.drawable.zzzz_iphone_note;
        } else if(cls.equals("com.yahoo.mobile.client.android.finance.activity.Main")){
        	return R.drawable.zzzz_iphone_economic;
        }
        return 0;
    }
    
    private int getDefaultTitleRes(String cls)
    {
        if(cls.equals("com.android.browser.BrowserActivity") 
        		|| cls.equals("com.boatgo.browser.BrowserActivity")){
            return R.string.app_browser;
        }else if(cls.equals("com.chaozh.iReader.ui.activity.WelcomeActivity")){
        	return R.string.app_ibooks;
        }else if(cls.equals("com.qishang.android.widget.MainActivity")
        		|| cls.equals("com.aedesign.iphoneweather.activity.LaunchActivity")
        		|| cls.equals("com.youba.WeatherForecast.WeatherForecastActivity")
        		|| cls.equals("com.iphonestyle.weather.activity.MainActivity")){
            return R.string.app_weather;
        }else if(cls.equals("com.android.vending.AssetBrowserActivity") 
        	|| cls.equals("com.uucun105294.android.cms.activity.MarketLoginAndRegisterActivity")
        	|| cls.equals("com.hiapk.marketpho.MarketMainFrame")){
            return R.string.app_store;
        } else if(cls.equals("com.cooliris.media.Gallery") 
        		|| cls.equals("com.android.camera.GalleryPicker")
        		|| cls.equals("com.silencecork.photography.activity.AlbumActivity")){
            return R.string.app_photos;
        } else if(cls.equals("com.baidu.BaiduMap.BaiduMap")) {
        	return R.string.app_maps;
        } else if(cls.equals("com.yahoo.mobile.client.android.finance.activity.Main")){
        	return R.string.app_stock;
        }
        
        return 0;
    }
    
    private boolean checkIsUseTheCustomIcon(String cls, String pkgName){
    	if(cls != null){
	    	if(cls.equals("com.tencent.qqmusic.AppStarterActivity")
	    		|| cls.equals("com.speedsoftware.rootexplorer.RootExplorer")
	    		|| cls.equals("com.android.compass.CompassActivity")
	    		|| cls.equals("com.baidu.BaiduMap.BaiduMap")
	    		|| cls.equals("com.yingyonghui.market.ActivitySplash")
	    		|| cls.equals("com.nlucas.notificationtoaster.NotificationToasterPreferences")
	    		|| cls.equals("com.moz.appstore.AppList")
	    		|| cls.equals("com.youku.phone.ActivityWelcome")
	    		|| cls.equals("com.sina.weibo.SplashActivity")
	    		|| cls.equals("com.netease.newsreader.activity.MainIndexActivity")
	    		|| cls.equals("com.cooguo.memo.ui.MemoMain")
	    		|| cls.equals("com.kukool.memo.ui.MemoMain")
	    		|| cls.equals("com.hskj.iphonecompass.CompassActivity")
	    		|| cls.equals("tv.pps.mobile.WelcomeActivity")
	    		|| cls.equals("com.tencent.WBlog.activity.WBlogFirstRun")
	    		|| cls.equals("com.tencent.mm.ui.LauncherUI")
	    		|| cls.equals("com.v5music.YYMusicMain")
	    		|| cls.equals("com.android.passbook.MainActivity")){
	            return true;
	        }
    	}
    	
    	if(pkgName != null){
    		if(pkgName.equals("com.facebook.katana")
    				|| pkgName.equals("com.antutu.ABenchMark")
    				|| pkgName.equals("com.droidhen.fruit")
    			){
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private boolean checkIsUseTheCustomIcon(String cls){
    	
    	return checkIsUseTheCustomIcon(cls, null);
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(ComponentName componentName) {
        synchronized (mCache) {
            mCache.remove(componentName);
        }
    }

    /**
     * Empty out the cache.
     */
    public void flush() {
        synchronized (mCache) {
            mCache.clear();
        }
    }

    /**
     * Fill in "application" with the icon and label for "info."
     */
    public void getTitleAndIcon(ApplicationInfo application, ResolveInfo info) {
        synchronized (mCache) {
            CacheEntry entry = cacheLocked(application.componentName, info);
            if (entry.titleBitmap == null) {
                entry.titleBitmap = mBubble.createTextBitmap(entry.title);
            }

            application.title = entry.title;
            application.titleBitmap = entry.titleBitmap;
            application.mIcon = entry.icon;
        }
    }
    
    public void getTitleAndIcon(AppShortcutInfo application, ResolveInfo info) {
        synchronized (mCache) {
            CacheEntry entry = cacheLocked(application.getComponent(), info);
            if (entry.titleBitmap == null) {
                entry.titleBitmap = mBubble.createTextBitmap(entry.title);
            }

            application.title = entry.title;
            //application.titleBitmap = entry.titleBitmap;
            application.mIcon = entry.icon;
        }
    }

    public Bitmap getIcon(Intent intent) {
        synchronized (mCache) {
            final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            ComponentName component = intent.getComponent();

            if (resolveInfo == null || component == null) {
                return mDefaultIcon;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo);
            return entry.icon;
        }
    }

    public Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo) {
        synchronized (mCache) {
            if (resolveInfo == null || component == null) {
                return null;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo);
            return entry.icon;
        }
    }

    public void changeSmsShortcutIcon(ComponentName component, Bitmap icon) {
        synchronized (mCache) {
            if (component == null || icon == null) {
                // Log.d("QiShang",
                // "IconCache::changeSmsShortcutIcon()==pama error==");
                return;
            }

            CacheEntry entry = mCache.get(component);
            if (entry != null) {
                entry.icon = icon;

                mCache.put(component, entry);
            }
            // else
            // {
            // Log.d("QiShang",
            // "IconCache::changeSmsShortcutIcon()==entry is null==");
            // }
        }
    }

    private CacheEntry cacheLocked(ComponentName componentName, ResolveInfo info) {
        CacheEntry entry = mCache.get(componentName);
        if (entry == null) {
            entry = new CacheEntry();

            mCache.put(componentName, entry);
            
            final String classname = componentName.getClassName();
            int nDefId = getDefaultTitleRes(classname);
            if(nDefId > 0){
            	entry.title = mContext.getString(nDefId);
            } else {
	            entry.title = info.loadLabel(mPackageManager).toString();
	            if (entry.title == null) {
	                entry.title = info.activityInfo.name;
	            }
            }
            
            nDefId = getDefaultIconRes(classname);
            //android.util.Log.d("QsLog", "cacheLocked()==nDefId:"+nDefId+"=="+componentName.toString());
            if(nDefId > 0){
            	//Bitmap icon = ((BitmapDrawable)mContext.getResources().getDrawable(nDefId)).getBitmap();
            	entry.icon = Utilities.createIconBitmapEx(mContext.getResources().getDrawable(nDefId), mContext);//createIconBitmap(icon, mContext);
            } else {
            	if(checkIsUseTheCustomIcon(classname, componentName.getPackageName())){
            		entry.icon = Utilities.createIconBitmapEx(info.activityInfo.loadIcon(mPackageManager),
		                    mContext);
            	} else {
		            entry.icon = Utilities.createIconBitmap(info.activityInfo.loadIcon(mPackageManager),
		                    mContext);
            	}
            }
        }
        return entry;
    }
}
