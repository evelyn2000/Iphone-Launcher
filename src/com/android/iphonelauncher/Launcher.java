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

import com.android.common.Search;

import com.android.ui.IphoneIndicator;
import com.android.ui.IphoneShortcutCallback;
import com.android.ui.SearchScreen;
import com.android.util.AnimManager;
import com.android.util.Common;

import android.R.integer;
import android.app.Activity;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.CallLog;
import android.provider.LiveFolders;
import android.provider.Telephony.MmsSms;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.method.TextKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.view.View.MeasureSpec;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.DataInputStream;
import java.lang.ref.WeakReference;

import com.mediatek.common.featureoption.FeatureOption;

//jz for sms
import android.provider.Telephony.Sms;

/**
 * Default launcher application.
 */
public final class Launcher extends Activity implements View.OnClickListener, OnLongClickListener,
        LauncherModel.Callbacks {
    public static final String TAG = "Launcher2.Launcher";

    static final boolean LOGD = false;

    static final boolean PROFILE_STARTUP = false;

    static final boolean DEBUG_WIDGETS = false;

    static final boolean DEBUG_USER_INTERFACE = false;

    private static final int WALLPAPER_SCREENS_SPAN = 2;

    private static final int MENU_GROUP_ADD = 1;

    private static final int MENU_GROUP_WALLPAPER = MENU_GROUP_ADD + 1;

    private static final int MENU_ADD = Menu.FIRST + 1;

    private static final int MENU_WALLPAPER_SETTINGS = MENU_ADD + 1;

    private static final int MENU_SEARCH = MENU_WALLPAPER_SETTINGS + 1;

    private static final int MENU_NOTIFICATIONS = MENU_SEARCH + 1;

    private static final int MENU_SETTINGS = MENU_NOTIFICATIONS + 1;

    private static final int REQUEST_CREATE_SHORTCUT = 1;

    private static final int REQUEST_CREATE_LIVE_FOLDER = 4;

    private static final int REQUEST_CREATE_APPWIDGET = 5;

    private static final int REQUEST_PICK_APPLICATION = 6;

    private static final int REQUEST_PICK_SHORTCUT = 7;

    private static final int REQUEST_PICK_LIVE_FOLDER = 8;

    private static final int REQUEST_PICK_APPWIDGET = 9;

    private static final int REQUEST_PICK_WALLPAPER = 10;

    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    static final String KEY_SCREEN_COUNT = "key_screen_count";

    static final int SCREEN_COUNT = 5;

    static final int DEFAULT_SCREEN = 2;

    static final int NUMBER_CELLS_X = 4;

    static final int NUMBER_CELLS_Y = 4;

    static final int DIALOG_CREATE_SHORTCUT = 1;

    static final int DIALOG_RENAME_FOLDER = 2;

    private static final String PREFERENCES = "launcher.preferences";

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";

    // Type: boolean
    private static final String RUNTIME_STATE_ALL_APPS_FOLDER = "launcher.all_apps_folder";

    // Type: long
    private static final String RUNTIME_STATE_USER_FOLDERS = "launcher.user_folder";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cellX";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cellY";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_spanX";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_spanY";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "launcher.add_countX";

    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "launcher.add_countY";

    // Type: int[]
    private static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "launcher.add_occupied_cells";

    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";

    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";

    private static final String RUNTIME_STATE_CURRENT_INDICATOR = "launcher.current_indicator";

    // private Indicator mIndicator;

    // private ImageView mPreview;

    // private boolean mIsPortrait = true;

    // private Bitmap mThumbnail = null;

    // static final float PREVIEW_SCALE = 100.0f / 320;

    // static final int PREVIEW_PADDING = 4;

    private DragLayer mDragLayer;

    // static final int APPWIDGET_HOST_ID = 1024;

    private static final Object sLock = new Object();

    private static int sScreen = DEFAULT_SCREEN;

    private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();

    // private final ContentObserver mWidgetObserver = new
    // AppWidgetResetObserver();

    private LayoutInflater mInflater;

    private DragController mDragController;

    private Workspace mWorkspace;

    // private AppWidgetManager mAppWidgetManager;

    // private LauncherAppWidgetHost mAppWidgetHost;

    // private CellLayout.CellInfo mAddItemCellInfo;

    // private CellLayout.CellInfo mMenuAddInfo;

    // private final int[] mCellCoordinates = new int[2];

    // private FolderInfo mFolderInfo;

    // private DeleteZone mDeleteZone;

    // private HandleView mHandleView;

    // private AllAppsView mAllAppsGrid;

    // private ImageView mHotseatLeft;

    // private ImageView mHotseatRight;

    private Bundle mSavedState;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;

    private boolean mPaused = true;
    private boolean mOnResumeNeedsLoad;

    private boolean mRestoring;

    private boolean mWaitingForResult;

    private Bundle mSavedInstanceState;

    private LauncherModel mModel;

    private IconCache mIconCache;

    private ArrayList<ItemInfo> mDesktopItems = new ArrayList<ItemInfo>();

    // private static HashMap<Long, FolderInfo> mFolders = new HashMap<Long,
    // FolderInfo>();

    // private ImageView mPreviousView;

    // private ImageView mNextView;

    // Hotseats (quick-launch icons next to AllApps)
    // private static final int NUM_HOTSEATS = 2;

    // private String[] mHotseatConfig = null;
    //
    // private Intent[] mHotseats = null;
    //
    // private Drawable[] mHotseatIcons = null;
    //
    // private CharSequence[] mHotseatLabels = null;

    // private QsWorkspacePreview mQsWorkspacePreview;

    private AnimManager mAnimManager;

    private PackageManager mPm;

    private Workspace mNavigatebar;

    private View mNavigateContainer;

    // private IphoneToolbar mToolbar;

    private IphoneIndicator mIphoneIndicator;

    private Dialog mLoadingDialog;

    private View mMmsAppView;

    private View mPhoneAppView;

    private View mCalendarAppView;

    // private boolean mIsA602 = false;

    private Animation mHideAnimation;

    private Animation mShowAnimation;

    private Handler mHandler = new Handler();
    private int mOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if(!IsAllowToRun())
        // {
        // finish();
        // return;
        // }

        LauncherApplication app = ((LauncherApplication) getApplication());
        mModel = app.setLauncher(this);
        mIconCache = app.getIconCache();
        mDragController = new DragController(this);
        mInflater = getLayoutInflater();

        // mAppWidgetManager = AppWidgetManager.getInstance(this);
        // mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        // mAppWidgetHost.startListening();
        //
        // mIsPortrait = isScreenPortrait();

        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing("/sdcard/iphonelauncher");
        }
        mOrientation = getResources().getConfiguration().orientation;
        // loadHotseats();
        boolean isLocaleChange = checkForLocaleChange();
//Log.d(TAG, "==onCreate()===isLocaleChange:"+isLocaleChange);
        mModel.setLocaleChange(isLocaleChange);

        setWallpaperDimension();

        setupViews();

        //
        InitSmsAndPhoneObservers();

        // registerContentObservers();

        // lockAllApps();

        mSavedState = savedInstanceState;
        restoreState(mSavedState);

        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }

        showHomeLoadingDialog();
        
        // if (!mRestoring) {
        mModel.setAllAppsDirty();
        mModel.startLoader(this, true);
        // }

        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);

        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        // jz
        filter.addAction("com.qishang.sms.UNREAD_COUNT_CHANGED");
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        // filter.addAction(Intent.ACTION_SCREEN_OFF);
        // filter.addAction(Intent.ACTION_SCREEN_ON);

        registerReceiver(mCloseSystemDialogsReceiver, filter);

        // qs_reset_lockscreenstle();

        //
        mAnimManager = AnimManager.getInstance();
        mPm = getPackageManager();

        initAnimation();
    }

    public boolean isScreenPortrait() {
    	return (mOrientation != Configuration.ORIENTATION_LANDSCAPE);
        /*DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        return screenHeight > screenWidth;*/
    }

    private boolean checkForLocaleChange() {
        final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
        readConfiguration(this, localeConfiguration);

        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = localeConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = localeConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = localeConfiguration.mnc;
        final int mnc = configuration.mnc;

        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc
                || mnc != previousMnc;

        if (localeChanged) {
            localeConfiguration.locale = locale;
            localeConfiguration.mcc = mcc;
            localeConfiguration.mnc = mnc;

            writeConfiguration(this, localeConfiguration);
            mIconCache.flush();
            return true;
        }
        return false;
    }

    private static class LocaleConfiguration {
        public String locale;

        public int mcc = -1;

        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }

    private void setWallpaperDimension() {
        WallpaperManager wpm = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);

        Display display = getWindowManager().getDefaultDisplay();
        // to support landscape, we mustn't do this
        // boolean isPortrait = display.getWidth() < display.getHeight();

        // final int width = isPortrait ? display.getWidth() :
        // display.getHeight();
        // final int height = isPortrait ? display.getHeight() :
        // display.getWidth();

        final int width = display.getWidth();
        final int height = display.getHeight();
        wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mWaitingForResult = false;

        // // The pattern used here is that a user PICKs a specific application,
        // // which, depending on the target, might need to CREATE the actual
        // // target.
        //
        // // For example, the user would PICK_SHORTCUT for "Music playlist",
        // and
        // // we
        // // launch over to the Music app to actually CREATE_SHORTCUT.
        //
        // if (resultCode == RESULT_OK && mAddItemCellInfo != null) {
        // switch (requestCode) {
        // case REQUEST_PICK_APPLICATION:
        // completeAddApplication(this, data, mAddItemCellInfo);
        // break;
        // case REQUEST_PICK_SHORTCUT:
        // processShortcut(data);
        // break;
        // case REQUEST_CREATE_SHORTCUT:
        // completeAddShortcut(data, mAddItemCellInfo);
        // break;
        // case REQUEST_PICK_LIVE_FOLDER:
        // addLiveFolder(data);
        // break;
        // case REQUEST_CREATE_LIVE_FOLDER:
        // completeAddLiveFolder(data, mAddItemCellInfo);
        // break;
        // case REQUEST_PICK_APPWIDGET:
        // addAppWidget(data);
        // break;
        // case REQUEST_CREATE_APPWIDGET:
        // completeAddAppWidget(data, mAddItemCellInfo);
        // break;
        // case REQUEST_PICK_WALLPAPER:
        // // We just wanted the activity result here so we can clear
        // // mWaitingForResult
        // break;
        // }
        // } else if ((requestCode == REQUEST_PICK_APPWIDGET || requestCode ==
        // REQUEST_CREATE_APPWIDGET)
        // && resultCode == RESULT_CANCELED && data != null) {
        // // Clean up the appWidgetId if we canceled
        // int appWidgetId =
        // data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        // if (appWidgetId != -1) {
        // mAppWidgetHost.deleteAppWidgetId(appWidgetId);
        // }
        // }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //android.util.Log.w("QsLog", "onResume()====");
        mPaused = false;
        //Log.d(TAG, "==onResume()===mRestoring:"+mRestoring);
        if(false){
	        if (mRestoring) {
	            // mWorkspaceLoading = true;
	            // mModel.startLoader(this, false);
	            mRestoring = false;
	        }
        } else {
	        if (mRestoring || mOnResumeNeedsLoad) {
	            mWorkspaceLoading = true;
	            mModel.startLoader(this, true);
	            mRestoring = false;
	            mOnResumeNeedsLoad = false;
	        }
        }
        
        mIsNewIntent = false;
        mDragController.cancelDrag();

        // if (mIndicator != null) {
        // mIndicator.reset();
        // if (mPreview != null && mPreview.getParent() != null) {
        // WindowManagerImpl.getDefault().removeView(mPreview);
        // }
        // mIndicator.postInvalidate();
        // }
        // android.util.Log.d("QsLog", "Launcher::onResume()===");
        /*
         * Intent intent = new Intent(Intent.ACTION_QS_STATUSBAR_SWITCH_BG);
         * intent.putExtra(Intent.EXTRA_TEXT,
         * Intent.ACTION_QS_STATUSBAR_SWITCH_BG_HOME);
         * super.sendBroadcast(intent);
         */
        // update sms phone icon
        updateSmsIconEx();
        updatePhoneIconEx();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mWorkspace != null) {
            mWorkspace.setCurrentScreen(getCurrentWorkspaceScreen());
        }

        // dismissPreview(mPreviousView);
        // dismissPreview(mNextView);
        mDragController.cancelDrag();
        /*
         * android.util.Log.d("QsLog",
         * "Launcher::onPause()===mQsIsScreenOn:"+mQsIsScreenOn); Intent intent
         * = new Intent(Intent.ACTION_QS_STATUSBAR_SWITCH_BG);
         * intent.putExtra(Intent.EXTRA_TEXT,
         * Intent.ACTION_QS_STATUSBAR_SWITCH_BG_OTHER);
         * super.sendBroadcast(intent);
         */
        dismissHomeLoadingDialog();
        closeIphoneFolder();
        mAnimManager.stop();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        Log.d(TAG, "onRetainNonConfigurationInstance ");
        // Flag the loader to stop early before switching
        mModel.stopLoader();
        // mAllAppsGrid.surrender();
        return Boolean.TRUE;
    }

    // We can't hide the IME if it was forced open. So don't bother
    /*
     * @Override public void onWindowFocusChanged(boolean hasFocus) {
     * super.onWindowFocusChanged(hasFocus); if (hasFocus) { final
     * InputMethodManager inputManager = (InputMethodManager)
     * getSystemService(Context.INPUT_METHOD_SERVICE);
     * WindowManager.LayoutParams lp = getWindow().getAttributes();
     * inputManager.hideSoftInputFromWindow(lp.token, 0, new
     * android.os.ResultReceiver(new android.os.Handler()) { protected void
     * onReceiveResult(int resultCode, Bundle resultData) { Log.d(TAG,
     * "ResultReceiver got resultCode=" + resultCode); } }); Log.d(TAG,
     * "called hideSoftInputFromWindow from onWindowFocusChanged"); } }
     */

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        if (!handled && acceptFilter() && keyCode != KeyEvent.KEYCODE_ENTER) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
                    keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                // something usable has been typed - start a search
                // the typed text will be retrieved and cleared by
                // showSearchDialog()
                // If there are multiple keystrokes before the search dialog
                // takes focus,
                // onSearchRequested() will be called for every keystroke,
                // but it is idempotent, so it's fine.
                return onSearchRequested();
            }
        }

        // Eat the long press event so the keyboard doesn't come up.
        // if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
        // return true;
        // }

        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Restores the previous state, if it exists.
     * 
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
        Log.d(TAG, "restoreState " + savedState);
        if (savedState == null) {
            return;
        }

        // final boolean allApps =
        // savedState.getBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, false);
        // if (allApps) {
        // showAllApps(false);
        // }

        final int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
        if (currentScreen > -1) {
            mWorkspace.setCurrentScreen(currentScreen);
            mIphoneIndicator.setCountAndIndex(mWorkspace.getChildCount(), currentScreen);
        }

        // final int addScreen =
        // savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
        // if (addScreen > -1) {
        // mAddItemCellInfo = new CellLayout.CellInfo();
        // final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
        // addItemCellInfo.valid = true;
        // addItemCellInfo.screen = addScreen;
        // addItemCellInfo.cellX =
        // savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
        // addItemCellInfo.cellY =
        // savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
        // addItemCellInfo.spanX =
        // savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
        // addItemCellInfo.spanY =
        // savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
        // addItemCellInfo.findVacantCellsFromOccupied(
        // savedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS),
        // savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_X),
        // savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y));
        // mRestoring = true;
        // }

        // boolean renameFolder =
        // savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        // if (renameFolder) {
        // long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
        // mFolderInfo = mModel.getFolderById(this, mFolders, id);
        //
        // }

        mRestoring = true;
    }

    // private AllAppsView getAllAppsGrid(DragLayer dragLayer) {
    // String allAppsGrid = SystemProperties.get("launcher2.allappsgrid", "2d");
    // ViewStub stub;
    // if (allAppsGrid.equals("2d")) {
    // stub = (ViewStub) dragLayer.findViewById(R.id.stub_all_apps_2d);
    // } else if (allAppsGrid.equals("3d_11")) {
    // stub = (ViewStub) dragLayer.findViewById(R.id.stub_all_apps_3d_11);
    // } else if (allAppsGrid.equals("3d_20")) {
    // stub = (ViewStub) dragLayer.findViewById(R.id.stub_all_apps_3d_20);
    // } else {
    // stub = (ViewStub) dragLayer.findViewById(R.id.all_apps_view_2d);
    // }
    //
    // return (AllAppsView) stub.inflate();
    // }

    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
        // mQsWorkspacePreview = (QsWorkspacePreview)
        // findViewById(R.id.wp_preview);
        final DragController dragController = mDragController;
        dragController.clearDropTarget();
        dragController.removeDragListener(null);
        
        setContentView(R.layout.launcher);

        final DragLayer dragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mDragLayer = dragLayer;
        dragLayer.setDragController(dragController);

        // mAllAppsGrid = getAllAppsGrid(dragLayer);
        //
        // mAllAppsGrid.setLauncher(this);
        // mAllAppsGrid.setDragController(dragController);
        // ((View) mAllAppsGrid).setWillNotDraw(false); // We don't want a hole
        // // punched in our window.
        // // Manage focusability manually since this thing is always visible
        // ((View) mAllAppsGrid).setFocusable(false);

        mWorkspace = (Workspace) dragLayer.findViewById(R.id.workspace);
        final Workspace workspace = mWorkspace;
        workspace.setHapticFeedbackEnabled(false);
        workspace.setOnLongClickListener(this);
        workspace.setDragController(dragController);
        workspace.setLauncher(this);

        //
        mNavigateContainer = dragLayer.findViewById(R.id.navigate_container);

        //
        mNavigatebar = (Workspace) dragLayer.findViewById(R.id.navigatebar);
        mNavigatebar.setHapticFeedbackEnabled(false);
        mNavigatebar.setOnLongClickListener(this);
        mNavigatebar.setDragController(dragController);
        mNavigatebar.setLauncher(this);

        //
        // mToolbar = (IphoneToolbar) dragLayer.findViewById(R.id.toobar);
        // mToolbar.setLauncher(this);
        //
        mIphoneIndicator = (IphoneIndicator) dragLayer.findViewById(R.id.iphoneindicator);

        workspace.setIndicators(mIphoneIndicator);

        // DeleteZone deleteZone = (DeleteZone)
        // dragLayer.findViewById(R.id.delete_zone);
        // mDeleteZone = deleteZone;

        // mHandleView = (HandleView) findViewById(R.id.all_apps_button);
        // mHandleView.setLauncher(this);
        // mHandleView.setOnClickListener(this);
        // mHandleView.setOnLongClickListener(this);
        //
        // mHotseatLeft = (ImageView) findViewById(R.id.hotseat_left);
        // mHotseatLeft.setContentDescription(mHotseatLabels[0]);
        // mHotseatLeft.setImageDrawable(mHotseatIcons[0]);
        //
        // mHotseatRight = (ImageView) findViewById(R.id.hotseat_right);
        // mHotseatRight.setContentDescription(mHotseatLabels[1]);
        // mHotseatRight.setImageDrawable(mHotseatIcons[1]);


        // String pro_name =
        // com.mediatek.featureoption.FeatureOption.Qs_Sub_Project_Name;
        // mIsA602 = pro_name.startsWith("A602");
        // if (mIsA602) {
        // mHotseatRight.setImageResource(R.drawable.hotseat_home);
        // }
        //
        // mPreviousView = (ImageView)
        // dragLayer.findViewById(R.id.previous_screen);
        // mNextView = (ImageView) dragLayer.findViewById(R.id.next_screen);
        //
        // if (!mIsPortrait) {
        // mPreviousView.setVisibility(View.GONE);
        // mNextView.setVisibility(View.GONE);
        // }
        //
        // Drawable previous = mPreviousView.getDrawable();
        // Drawable next = mNextView.getDrawable();
        // mWorkspace.setIndicators(previous, next);
        //
        // mPreviousView.setHapticFeedbackEnabled(false);
        // mPreviousView.setOnLongClickListener(this);
        // mNextView.setHapticFeedbackEnabled(false);
        // mNextView.setOnLongClickListener(this);
        //
        // deleteZone.setLauncher(this);
        // deleteZone.setDragController(dragController);
        // deleteZone.setHandle(findViewById(R.id.all_apps_button_cluster));

        dragController.setDragScoller(workspace);
        // dragController.setDragListener(deleteZone);
        dragController.setScrollView(dragLayer);
        dragController.setMoveTarget(workspace);

        // The order here is bottom to top.
        dragController.addDropTarget(workspace);
        // dragController.addDropTarget(deleteZone);
        dragController.addDropTarget(mNavigatebar);

        dragController.setLauncher(this);

        /* for landscape mode indicator */
        // mIndicator = (Indicator) findViewById(R.id.indicator);
        // mIndicator.setLauncher(this);
        // Bitmap indicatorBmp = BitmapFactory.decodeResource(getResources(),
        // R.drawable.indicator_background);
        // mIndicator.getLayoutParams().width = indicatorBmp.getWidth();
        // mIndicator.setVisibility(View.GONE);
        // mIndicator.setClickable(false);
        // mIndicator.setFocusable(false);
    }

    // @SuppressWarnings({
    // "UnusedDeclaration"
    // })
    // public void previousScreen(View v) {
    // if (!isAllAppsVisible()) {
    // mWorkspace.scrollLeft();
    // }
    // }
    //
    // @SuppressWarnings({
    // "UnusedDeclaration"
    // })
    // public void nextScreen(View v) {
    // if (!isAllAppsVisible()) {
    // mWorkspace.scrollRight();
    // }
    // }

    // @SuppressWarnings({
    // "UnusedDeclaration"
    // })
    // public void launchHotSeat(View v) {
    // if (isAllAppsVisible())
    // return;
    //
    // int index = -1;
    // if (v.getId() == R.id.hotseat_left) {
    // index = 0;
    // } else if (v.getId() == R.id.hotseat_right) {
    // index = 1;
    // }
    // if (index == 0 && FeatureOption.QsDialStyle_Type ==
    // FeatureOption.QsUiStyle_Htc) {
    // Intent dialIntent = new Intent(Intent.ACTION_DIAL);
    // dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // startActivity(dialIntent);
    // return;
    // }
    // if (index == 1 && mIsA602) {
    // startActivity(new Intent(Intent.ACTION_MAIN).addFlags(
    // Intent.FLAG_ACTIVITY_NEW_TASK |
    // Intent.FLAG_ACTIVITY_CLEAR_TOP).addCategory(
    // Intent.CATEGORY_HOME));
    // return;
    // }
    // // reload these every tap; you never know when they might change
    // loadHotseats();
    // if (index >= 0 && index < mHotseats.length && mHotseats[index] != null) {
    // Intent intent = mHotseats[index];
    // startActivitySafely(mHotseats[index], "hotseat");
    // }
    // }

    // /**
    // * Creates a view representing a shortcut.
    // *
    // * @param info The data structure describing the shortcut.
    // * @return A View inflated from R.layout.application.
    // */
    // View createShortcut(ShortcutInfo info) {
    // return createShortcut(R.layout.application,
    // (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
    // }

    /**
     * Creates a view representing a shortcut inflated from the specified
     * resource.
     * 
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, AppShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
        favorite.setIphoneIcon(info.getIcon(mIconCache));
        favorite.setText(info.title);
        favorite.setTag(info);
        favorite.setOnClickListener(this);
        return favorite;
    }


    void closeSystemDialogs() {
        getWindow().closeAllPanels();

        // try {
        // dismissDialog(DIALOG_CREATE_SHORTCUT);
        // // Unlock the workspace if the dialog was showing
        // } catch (Exception e) {
        // // An exception is thrown if the dialog is not visible, which is
        // // fine
        // }
        //
        // try {
        // dismissDialog(DIALOG_RENAME_FOLDER);
        // // Unlock the workspace if the dialog was showing
        // } catch (Exception e) {
        // // An exception is thrown if the dialog is not visible, which is
        // // fine
        // }

        // Whatever we were doing is hereby canceled.
        mWaitingForResult = false;
    }

    protected void qs_reset_lockscreenstle() {
        // jz
        /*
         * int lockid = SystemProperties.getInt("persist.qs.lockscreen", 0); //
         * Log.v("QsLog", //
         * "===launcher2::qs_reset_lockscreenstle===lockid:"+lockid + "=="); if
         * (com.mediatek.featureoption.FeatureOption.QsUiStyle_Defalut !=
         * lockid) { SystemProperties.set("persist.qs.lockscreen",
         * com.mediatek.featureoption.FeatureOption.QsUiStyle_Defalut + "");
         * sendBroadcast(new Intent("android.intent.qs.LOCKSCREEN_CHANGED")); }
         */
    }

    // private void QsWorkspacePreviewStatus(boolean bIsShow) {
    // int screenCount = mWorkspace.getChildCount();
    // for (int screen = 0; screen < screenCount; screen++) {
    // final CellLayout v = (CellLayout) mWorkspace.getChildAt(screen);
    // v.setDrawingCacheEnabled(true);
    // v.setAlwaysDrawnWithCacheEnabled(true);
    // }
    //
    // if (!bIsShow) {
    // mQsWorkspacePreview.setVisibility(View.INVISIBLE);
    // // mWorkspace.clearChildrenCache();
    // } else {
    // mQsWorkspacePreview.setVisibility(View.VISIBLE);
    // mQsWorkspacePreview.setWorkspace(mWorkspace);
    // mQsWorkspacePreview.bringToFront();
    // mQsWorkspacePreview.requestFocus();
    // }
    // }

    private boolean mIsNewIntent = false;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Log.v("QsLog", "===launcher2::onNewIntent===1==");
        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {

            if (mAnimManager != null && mAnimManager.isAnim()) {
                mAnimManager.stop();
                return;
            }

            mIsNewIntent = true;

            // also will cancel mWaitingForResult.
            closeSystemDialogs();
            // Log.v("QsLog", "===launcher2::onNewIntent===2==");
            // qs_reset_lockscreenstle();

            // if (mToolbar != null) {
            // if (mToolbar.isOpen()) {
            // mToolbar.close();
            // } else {
            // mToolbar.open();
            // }
            // }

            // need to close the open folder
            closeIphoneFolder();

            boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            // boolean allAppsVisible = isAllAppsVisible();
            if (!mWorkspace.isDefaultScreenShowing() || !alreadyOnHome) {
                mWorkspace.moveToDefaultScreen(alreadyOnHome);
            } else {

                // QsWorkspacePreviewStatus(!mQsWorkspacePreview.isShown());

                /*
                 * if (mQsWorkspacePreview.isShown()) {
                 * mQsWorkspacePreview.setVisibility(View.INVISIBLE); } else {
                 * mQsWorkspacePreview.setVisibility(View.VISIBLE);
                 * mQsWorkspacePreview.setWorkspace(mWorkspace);
                 * mQsWorkspacePreview.bringToFront();
                 * mQsWorkspacePreview.requestFocus(); }
                 */
            }
            // closeAllApps(alreadyOnHome && allAppsVisible);

            final View v = getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Do not call super here
        Log.d(TAG, "onRestoreInstanceState ");
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState ");
        outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getCurrentScreen());
        // outState.putInt(RUNTIME_STATE_CURRENT_INDICATOR,
        // mIndicator.getCurrentIndex());

        final UserFolder folder = getOpenIphoneFolder();
        if (folder != null) {
            // final int count = folders.size();
            // long[] ids = new long[count];
            // for (int i = 0; i < count; i++) {
            // final FolderInfo info = folders.get(i).getInfo();
            // ids[i] = info.id;
            // }
            outState.putLongArray(RUNTIME_STATE_USER_FOLDERS, new long[] {
                folder.mInfo.id
            });
        } else {
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        // try {
        // mAppWidgetHost.stopListening();
        // } catch (NullPointerException ex) {
        // Log.w(TAG,
        // "problem while stopping AppWidgetHost during Launcher destruction",
        // ex);
        // }

        TextKeyListener.getInstance().release();

        mModel.stopLoader();

        unbindDesktopItems();

        // getContentResolver().unregisterContentObserver(mWidgetObserver);

        unInitSmsAndPhoneObserver();
        // dismissPreview(mPreviousView);
        // dismissPreview(mNextView);

        unregisterReceiver(mCloseSystemDialogsReceiver);
        
        dismissHomeLoadingDialog();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0)
            mWaitingForResult = true;
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {

        // closeAllApps(true);

        if (initialQuery == null) {
            // Use any text typed in the launcher as the initial query
            initialQuery = getTypedText();
            clearTypedText();
        }
        if (appSearchData == null) {
            appSearchData = new Bundle();
            appSearchData.putString(Search.SOURCE, "launcher-search");
        }

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchManager.startSearch(initialQuery, selectInitialQuery, getComponentName(),
                appSearchData, globalSearch);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isWorkspaceLocked()) {
            return false;
        }

        super.onCreateOptionsMenu(menu);

        // menu.add(MENU_GROUP_ADD, MENU_ADD, 0, R.string.menu_add)
        // .setIcon(android.R.drawable.ic_menu_add).setAlphabeticShortcut('A');
        // menu.add(MENU_GROUP_WALLPAPER, MENU_WALLPAPER_SETTINGS, 0,
        // R.string.menu_wallpaper)
        // .setIcon(android.R.drawable.ic_menu_gallery).setAlphabeticShortcut('W');
        // menu.add(0, MENU_SEARCH, 0, R.string.menu_search)
        // .setIcon(android.R.drawable.ic_search_category_default)
        // .setAlphabeticShortcut(SearchManager.MENU_KEY);
        // menu.add(0, MENU_NOTIFICATIONS, 0, R.string.menu_notifications)
        // .setIcon(com.android.internal.R.drawable.ic_menu_notifications)
        // .setAlphabeticShortcut('N');
        //
        // final Intent settings = new
        // Intent(android.provider.Settings.ACTION_SETTINGS);
        // settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
        // Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //
        // menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
        // .setIcon(android.R.drawable.ic_menu_preferences).setAlphabeticShortcut('P')
        // .setIntent(settings);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //
        // // If all apps is animating, don't show the menu, because we don't
        // know
        // // which one to show.
        // if (mAllAppsGrid.isVisible() && !mAllAppsGrid.isOpaque()) {
        // return false;
        // }
        //
        // // Only show the add and wallpaper options when we're not in all
        // apps.
        // boolean visible = !mAllAppsGrid.isOpaque();
        // menu.setGroupVisible(MENU_GROUP_ADD, visible);
        // menu.setGroupVisible(MENU_GROUP_WALLPAPER, visible);
        //
        // // Disable add if the workspace is full.
        // if (visible) {
        // mMenuAddInfo = mWorkspace.findAllVacantCells(null);
        // menu.setGroupEnabled(MENU_GROUP_ADD, mMenuAddInfo != null &&
        // mMenuAddInfo.valid);
        // }
        //startWallpaper();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // switch (item.getItemId()) {
        // case MENU_ADD:
        // addItems();
        // return true;
        // case MENU_WALLPAPER_SETTINGS:
        // startWallpaper();
        // return true;
        // case MENU_SEARCH:
        // onSearchRequested();
        // return true;
        // case MENU_NOTIFICATIONS:
        // showNotifications();
        // return true;
        // }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Indicates that we want global search for this activity by setting the
     * globalSearch argument for {@link #startSearch} to true.
     */

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        return true;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult;
    }

    private void startWallpaper() {
        // closeAllApps(true);
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper, getText(R.string.chooser_wallpaper));
        // NOTE: Adds a configure option to the chooser if the wallpaper
        // supports it
        // Removed in Eclair MR1
        // WallpaperManager wm = (WallpaperManager)
        // getSystemService(Context.WALLPAPER_SERVICE);
        // WallpaperInfo wi = wm.getWallpaperInfo();
        // if (wi != null && wi.getSettingsActivity() != null) {
        // LabeledIntent li = new LabeledIntent(getPackageName(),
        // R.string.configure_wallpaper, 0);
        // li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
        // chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
        // }
        startActivityForResult(chooser, REQUEST_PICK_WALLPAPER);
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    // private void registerContentObservers() {
    // ContentResolver resolver = getContentResolver();
    // resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI,
    // true,
    // mWidgetObserver);
    // InitShowUnReadedSmsCount();
    // }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (SystemProperties.getInt("debug.launcher2.dumpstate", 0) != 0) {
                        dumpState();
                        return true;
                    }
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    // if (mQsWorkspacePreview.isShown()) {
                    // // mQsWorkspacePreview.setVisibility(View.INVISIBLE);
                    // QsWorkspacePreviewStatus(false);
                    // }
                    break;
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // if (isAllAppsVisible()) {
        // closeAllApps(true);
        // } else {
        // closeFolder();
        // }
        // dismissPreview(mPreviousView);
        // dismissPreview(mNextView);

        if (mAnimManager != null && mAnimManager.isAnim()) {
            mAnimManager.stop();
            return;
        }

        if (closeIphoneFolder()) {
            return;
        }

        if (mWorkspace.getCurrentScreen() == 0) {
            mWorkspace.snapToScreen(1);
        } else if (mWorkspace.getCurrentScreen() == 1) {
            mWorkspace.snapToScreen(0);
        } else {
            mWorkspace.snapToScreen(1);
        }
    }

    // private void closeFolder() {
    // Folder folder = mWorkspace.getOpenFolder();
    // if (folder != null) {
    // closeFolder(folder);
    // }
    // }

    // void closeFolder(Folder folder) {
    // folder.getInfo().opened = false;
    // ViewGroup parent = (ViewGroup) folder.getParent();
    // if (parent != null) {
    // parent.removeView(folder);
    // if (folder instanceof DropTarget) {
    // // Live folders aren't DropTargets.
    // mDragController.removeDropTarget((DropTarget) folder);
    // }
    // }
    // folder.onClose();
    // }

    /**
     * Re-listen when widgets are reset.
     */
    // private void onAppWidgetReset() {
    // mAppWidgetHost.startListening();
    // }

    /**
     * Go through the and disconnect any of the callbacks in the drawables and
     * the views or we leak the previous Home screen on orientation change.
     */
    private void unbindDesktopItems() {
        for (ItemInfo item : mDesktopItems) {
            item.unbind();
        }
    }

    /**
     * Launches the intent referred by the clicked shortcut.
     * 
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
            if (AnimManager.getInstance().isAnim()) {
                return;
            }
            // if the shortcut is in an open folder,close it.
            closeIphoneFolder();
            // Open shortcut
            final Intent intent = ((ShortcutInfo) tag).intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0] + v.getWidth(), pos[1]
                    + v.getHeight()));
            startActivitySafely(intent, tag);
        } else if (tag instanceof FolderInfo) {
            // handleFolderClick((FolderInfo) tag);
            handleIphoneFolderClick((FolderInfo) tag, v);
        }
    }

    void startActivitySafely(Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent
                    + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                    + "or use the exported attribute for this activity. " + "tag=" + tag
                    + " intent=" + intent, e);
        }
    }

    void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent
                    + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                    + "or use the exported attribute for this activity.", e);
        }
    }

    // private void handleFolderClick(FolderInfo folderInfo) {
    // if (!folderInfo.opened) {
    // // Close any open folder
    // closeFolder();
    // // Open the requested folder
    // openFolder(folderInfo);
    // } else {
    // // Find the open folder...
    // Folder openFolder = mWorkspace.getFolderForTag(folderInfo);
    // int folderScreen;
    // if (openFolder != null) {
    // folderScreen = mWorkspace.getScreenForView(openFolder);
    // // .. and close it
    // closeFolder(openFolder);
    // if (folderScreen != mWorkspace.getCurrentScreen()) {
    // // Close any folder open on the current screen
    // closeFolder();
    // // Pull the folder onto this screen
    // openFolder(folderInfo);
    // }
    // }
    // }
    // }

    /**
     * Opens the user fodler described by the specified tag. The opening of the
     * folder is animated relative to the specified View. If the View is null,
     * no animation is played.
     * 
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    // private void openFolder(FolderInfo folderInfo) {
    // Folder openFolder;
    //
    // if (folderInfo instanceof UserFolderInfo) {
    // openFolder = UserFolder.fromXml(this);
    // } else if (folderInfo instanceof LiveFolderInfo) {
    // openFolder = com.android.iphonelauncher.LiveFolder.fromXml(this,
    // folderInfo);
    // } else {
    // return;
    // }
    //
    // openFolder.setDragController(mDragController);
    // openFolder.setLauncher(this);
    //
    // // openFolder.bind(folderInfo);
    // folderInfo.opened = true;
    //
    // mWorkspace.addInScreen(openFolder, folderInfo.screen, 0, 0, 4, 4);
    // openFolder.onOpen();
    // }

    public boolean onLongClick(View v) {
        // switch (v.getId()) {
        // case R.id.previous_screen:
        // if (!isAllAppsVisible()) {
        // mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
        // HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        // showPreviews(v);
        // }
        // return true;
        // case R.id.next_screen:
        // if (!isAllAppsVisible()) {
        // mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
        // HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        // showPreviews(v);
        // }
        // return true;
        // case R.id.all_apps_button:
        // if (!isAllAppsVisible()) {
        // mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
        // HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        // showPreviews(v);
        // }
        // return true;
        // }

        if (isWorkspaceLocked()) {
            return false;
        }

        if (v instanceof SearchScreen) {
            //startWallpaper(); //lijia delete for a631w; customer no need to ues long press to change wallpaper
            return true;
        }

        if (!(v instanceof CellLayout)) {
            v = (View) v.getParent();
        }

        CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();

        // This happens when long clicking an item with the dpad/trackball
        if (cellInfo == null) {
            return true;
        }
        Workspace workspace = (Workspace) v.getParent();
        if (workspace.allowLongPress()) {
            if (cellInfo.cell == null) {
                if (cellInfo.valid) {
                    // User long pressed on empty space
                    workspace.setAllowLongPress(false);
                    workspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    // showAddDialog(cellInfo);
                    //startWallpaper();//lijia delete for a631w; customer no need to ues long press to change wallpaper
                }
            } else {
                if (!(cellInfo.cell instanceof Folder)) {
                    ItemInfo info = (ItemInfo) cellInfo.cell.getTag();
                    // User long pressed on an item
                    if (!mAnimManager.isAnim()) {
                        mAnimManager.start();
                    }
                    if (info.container > 0) {
                        // it is in folder
                        Folder folder = getOpenIphoneFolder();
                        if (folder != null) {
                            folder.startDrag(cellInfo.cell);
                        }
                    } else {
                        // it is in workspace or navigatebar
                        workspace.startDrag(cellInfo);
                    }
                }
            }
        }
        return true;
    }

    // @SuppressWarnings({
    // "unchecked"
    // })
    // private void dismissPreview(final View v) {
    // final PopupWindow window = (PopupWindow) v.getTag();
    // if (window != null) {
    // window.setOnDismissListener(new PopupWindow.OnDismissListener() {
    // public void onDismiss() {
    // ViewGroup group = (ViewGroup) v.getTag(R.id.workspace);
    // int count = group.getChildCount();
    // for (int i = 0; i < count; i++) {
    // ((ImageView) group.getChildAt(i)).setImageDrawable(null);
    // }
    // ArrayList<Bitmap> bitmaps = (ArrayList<Bitmap>) v.getTag(R.id.icon);
    // for (Bitmap bitmap : bitmaps)
    // bitmap.recycle();
    //
    // v.setTag(R.id.workspace, null);
    // v.setTag(R.id.icon, null);
    // window.setOnDismissListener(null);
    // }
    // });
    // window.dismiss();
    // }
    // v.setTag(null);
    // }
    // private void showPreviews(View anchor) {
    // showPreviews(anchor, 0, mWorkspace.getChildCount());
    // }
    // private void showPreviews(final View anchor, int start, int end) {
    // final Resources resources = getResources();
    // final Workspace workspace = mWorkspace;
    //
    // CellLayout cell = ((CellLayout) workspace.getChildAt(start));
    //
    // float max = workspace.getChildCount();
    //
    // final Rect r = new Rect();
    // resources.getDrawable(R.drawable.preview_backgroundnoprogressive).getPadding(r);
    // int extraW = (int) ((r.left + r.right) * max);
    // int extraH = r.top + r.bottom;
    //
    // int aW = cell.getWidth() - extraW;
    // float w = aW / max;
    //
    // int width = cell.getWidth();
    // int height = cell.getHeight();
    // int x = cell.getLeftPadding();
    // int y = cell.getTopPadding();
    // width -= (x + cell.getRightPadding());
    // height -= (y + cell.getBottomPadding());
    //
    // float scale = w / width;
    //
    // int count = end - start;
    //
    // final float sWidth = width * scale;
    // float sHeight = height * scale;
    //
    // LinearLayout preview = new LinearLayout(this);
    //
    // PreviewTouchHandler handler = new PreviewTouchHandler(anchor);
    // ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(count);
    //
    // for (int i = start; i < end; i++) {
    // ImageView image = new ImageView(this);
    // cell = (CellLayout) workspace.getChildAt(i);
    //
    // final Bitmap bitmap = Bitmap.createBitmap((int) sWidth, (int) sHeight,
    // Bitmap.Config.ARGB_8888);
    //
    // final Canvas c = new Canvas(bitmap);
    // c.scale(scale, scale);
    // c.translate(-cell.getLeftPadding(), -cell.getTopPadding());
    // cell.dispatchDraw(c);
    //
    // image.setBackgroundDrawable(resources
    // .getDrawable(R.drawable.preview_backgroundnoprogressive));
    // image.setImageBitmap(bitmap);
    // image.setTag(i);
    // image.setOnClickListener(handler);
    // image.setOnFocusChangeListener(handler);
    // image.setFocusable(true);
    // if (i == mWorkspace.getCurrentScreen())
    // image.requestFocus();
    //
    // preview.addView(image, LinearLayout.LayoutParams.WRAP_CONTENT,
    // LinearLayout.LayoutParams.WRAP_CONTENT);
    //
    // bitmaps.add(bitmap);
    // }
    //
    // final PopupWindow p = new PopupWindow(this);
    // p.setContentView(preview);
    // p.setWidth((int) (sWidth * count + extraW));
    // p.setHeight((int) (sHeight + extraH));
    // p.setAnimationStyle(R.style.AnimationPreview);
    // p.setOutsideTouchable(true);
    // p.setFocusable(true);
    // p.setBackgroundDrawable(new ColorDrawable(0));
    // p.showAsDropDown(anchor, 0, 0);
    //
    // p.setOnDismissListener(new PopupWindow.OnDismissListener() {
    // public void onDismiss() {
    // dismissPreview(anchor);
    // }
    // });
    //
    // anchor.setTag(p);
    // anchor.setTag(R.id.workspace, preview);
    // anchor.setTag(R.id.icon, bitmaps);
    // }
    // class PreviewTouchHandler implements View.OnClickListener, Runnable,
    // View.OnFocusChangeListener {
    // private final View mAnchor;
    //
    // public PreviewTouchHandler(View anchor) {
    // mAnchor = anchor;
    // }
    //
    // public void onClick(View v) {
    // mWorkspace.snapToScreen((Integer) v.getTag());
    // v.post(this);
    // }
    //
    // public void run() {
    // dismissPreview(mAnchor);
    // }
    //
    // public void onFocusChange(View v, boolean hasFocus) {
    // if (hasFocus) {
    // mWorkspace.snapToScreen((Integer) v.getTag());
    // }
    // }
    // }

    Workspace getWorkspace() {
        return mWorkspace;
    }



    /**
     * Things to test when changing this code. - Home from workspace - from
     * center screen - from other screens - Home from all apps - from center
     * screen - from other screens - Back from all apps - from center screen -
     * from other screens - Launch app from workspace and quit - with back -
     * with home - Launch app from all apps and quit - with back - with home -
     * Go to a screen that's not the default, then all apps, and launch and app,
     * and go back - with back -with home - On workspace, long press power and
     * go back - with back - with home - On all apps, long press power and go
     * back - with back - with home - On workspace, power off - On all apps,
     * power off - Launch an app and turn off the screen while in that app - Go
     * back with home key - Go back with back key TODO: make this not go to
     * workspace - From all apps - From workspace - Enter and exit car mode
     * (becuase it causes an extra configuration changed) - From all apps - From
     * the center workspace - From another workspace
     */
    // void closeAllApps(boolean animated) {
    // if (mAllAppsGrid.isVisible()) {
    // showDragLayerElement(true);
    //
    // mWorkspace.hideWallpaper(false);
    // mWorkspace.setVisibility(View.VISIBLE);
    // mAllAppsGrid.zoom(0.0f, animated);
    // ((View) mAllAppsGrid).setFocusable(false);
    // mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
    //
    // // if (!mIsPortrait) {
    // // mIndicator.setVisibility(View.INVISIBLE);
    // // }
    // }
    // }

    void lockAllApps() {
        // TODO
    }

    void unlockAllApps() {
        // TODO
    }

    /**
     * Receives notifications when applications are added/removed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.qishang.sms.UNREAD_COUNT_CHANGED")) {
                Log.d("QsLog", "QsIphoneLauncher====UNREAD_COUNT_CHANGED=======");
                mShortcutUpdateHandler.sendEmptyMessage(SMS_ICON_UPDATE_MSG);
                return;
            }
            if (action.equals(Intent.ACTION_TIME_CHANGED)
                    || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                updateCalendarIcon();
                return;
            } else if (action.equals(Intent.ACTION_TIME_TICK)) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int min = calendar.get(Calendar.MINUTE);
                if (hour == 1 && min == 1) {
                    updateCalendarIcon();
                }
                return;
            }
            /*
             * if(action.equals(Intent.ACTION_SCREEN_OFF)) { Log.d("QsLog",
             * "QsIphoneLauncher====ACTION_SCREEN_OFF======="+mQsIsScreenOn);
             * mQsIsScreenOn = false; return; }
             * if(action.equals(Intent.ACTION_SCREEN_ON)) { Log.d("QsLog",
             * "QsIphoneLauncher====ACTION_SCREEN_OFF======="+mQsIsScreenOn);
             * mQsIsScreenOn = true; return; }
             */

            closeSystemDialogs();
            String reason = intent.getStringExtra("reason");
            if (!"homekey".equals(reason)) {
                boolean animate = true;
                if (mPaused || "lock".equals(reason)) {
                    animate = false;
                }
                // closeAllApps(animate);
            }
        }
    }

    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    // private class AppWidgetResetObserver extends ContentObserver {
    // public AppWidgetResetObserver() {
    // super(new Handler());
    // }
    //
    // @Override
    // public void onChange(boolean selfChange) {
    // onAppWidgetReset();
    // }
    // }
    
    public void showHomeLoadingDialog() {
        if (mLoadingDialog == null) {
        	mLoadingDialog = new Dialog(this);
        	mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        	mLoadingDialog.setContentView(R.layout.progressbar);
        	mLoadingDialog.setCancelable(false);
        	mLoadingDialog.show();
        }
    }
    
    public void dismissHomeLoadingDialog(){
    	if (mLoadingDialog != null) {
            try {
            	mLoadingDialog.dismiss();
            } catch (Exception e) {
                // We catch exception here, because have no impact on user
                Log.e(TAG, "Exception when Dialog.dismiss()...");
            } finally {
            	mLoadingDialog = null;
            }
        }
    }
    
    public boolean setLoadOnResume() {
        if (mPaused) {
            Log.i(TAG, "setLoadOnResume");
            mOnResumeNeedsLoad = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentScreen();
        } else {
            return 1;
        }
    }

    /**
     * Refreshes the shortcuts shown on the workspace. Implementation of the
     * method from LauncherModel.Callbacks.
     */
    public void startBinding() {
        final Workspace workspace = mWorkspace;
        int count = workspace.getChildCount();
        Log.d(TAG, "startBinding()====");
        for (int i = 1; i < count; i++) {
            // Use removeAllViewsInLayout() to avoid an extra requestLayout()
            // and invalidate().
            ((ViewGroup) workspace.getChildAt(i)).removeAllViewsInLayout();
        }

        // add by zf -qs
        ((ViewGroup) mNavigatebar.getChildAt(0)).removeAllViewsInLayout();

        // create new cell layout if needed.
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        int screen_count = sp.getInt(KEY_SCREEN_COUNT, SCREEN_COUNT);
//        if (screen_count > count) {
//            int screens = screen_count - count;
//            Log.d(TAG, "need to add  " + screens + " celllayouts");
//            while (screens > 0) {
//                CellLayout layout = (CellLayout) mInflater.inflate(R.layout.workspace_screen,
//                        workspace, false);
//                workspace.addView(layout, -1);
//                screens--;
//            }
//            mIphoneIndicator.setCountAndIndex(workspace.getChildCount(),
//                    workspace.getCurrentScreen());
//        }

        if (DEBUG_USER_INTERFACE) {
            android.widget.Button finishButton = new android.widget.Button(this);
            finishButton.setText("Finish");
            workspace.addInScreen(finishButton, 1, 0, 0, 1, 1);

            finishButton.setOnClickListener(new android.widget.Button.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // clear desktop items
        if (mDesktopItems != null) {
            mDesktopItems.clear();
        }

        mWorkspaceLoading = true;
    }

    /**
     * Bind the items start-end from the list. Implementation of the method from
     * LauncherModel.Callbacks.
     */
    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
    	setLoadOnResume();
    	
        final Workspace workspace = mWorkspace;
        final Workspace navigetWorkspace = mNavigatebar;
        //Log.w(TAG, "Launcher.bindItems()===start:"+start+"==end:"+end);
        
        synchronized (LauncherModel.mObject) {
            if (shortcuts.size() < end) {
                Log.e(TAG,
                        "Launcher.bindItems exit without bind. because siez is " + shortcuts.size()
                                + ", and end is " + end);
                return;
            }

            for (int i = start; i < end; i++) {
                final ItemInfo item = shortcuts.get(i);
                
                if(isDesktopCached(item)){
                	Log.e(TAG, "bindItems()===desktopcached=="+item.toString());
                	continue;
                }
                
                // cache info
                mDesktopItems.add(item);
                
                switch (item.itemType) {
            	case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:{
                	AppShortcutInfo info = (AppShortcutInfo) item;
                	if(item instanceof ApplicationInfo)
                		info = ((ApplicationInfo)item).makeShortcut();
                
                    //final AppShortcutInfo info = (AppShortcutInfo) item;
                    View shortcut = createIphoneShortcut(info);
                    switch ((int) item.container) {
                        case LauncherSettings.Favorites.CONTAINER_DESKTOP:{
                        	checkAndAddNewLayout(item.screen);
                        	CellLayout layout = (CellLayout) workspace.getChildAt(item.screen);
                        	if(layout.isEmptyCell(item.cellX, item.cellY)){
                                workspace.addInScreen(shortcut, item.screen, item.cellX,
                                        item.cellY, 1, 1, false);
                        	} else {
//                        		Log.e(TAG, "bindItems(DESKTOP)=app=no empty==x:"+item.cellX
//                        				+"==y:"+item.cellY+"==screen:"+item.screen);
                        		
                        		addOrUpdateAppInScreen(shortcut, info, false, 2);
                        	}
                        	break;
                        }
                        case LauncherSettings.Favorites.CONTAINER_NAVIGATEBAR: {
                            CellLayout layout = (CellLayout) navigetWorkspace.getChildAt(0);
                            int[] vacant = new int[2];
                            //if (layout.getVacantCell(vacant, 1, 1)) {
                            if(layout.isEmptyCell(item.cellX, item.cellY)){
//                                    navigetWorkspace.addInScreen(shortcut, 0, vacant[0], vacant[1],
//                                            1, 1, false);
                                
                                navigetWorkspace.addInScreen(shortcut, 0, item.cellX, item.cellY,
                                        1, 1, false);
                            } else if(layout.getVacantCell(vacant, 1, 1)){
                            	
                            	navigetWorkspace.addInScreen(shortcut, 0, vacant[0], vacant[1],
                                        1, 1, false);
                            	
                            	LauncherModel.addOrMoveItemInDatabase(this, info,
                                        LauncherSettings.Favorites.CONTAINER_NAVIGATEBAR, 0, vacant[0],
                                        vacant[1]);
                            } else {
//                            	Log.e(TAG, "bindItems(BAR)=app=no empty==x:"+item.cellX
//                        				+"==y:"+item.cellY+"==screen:"+item.screen);
                            	addOrUpdateAppInScreen(shortcut, info, false, 2);
                            }
                            break;
                        }
                        case ItemInfo.NO_ID:{
                        	addOrUpdateAppInScreen(shortcut, info, false, 2);
                        	break;
                        }
                        default:
                        	Log.e(TAG, "bindItems(app)==container error==="+item.toString());
                            break;
                    }
                    break;
                }
                case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
                    // if have no content,delete it.
                    UserFolderInfo folderInfo = (UserFolderInfo) item;
                    //Log.e(TAG, "bindItems(folder)==="+folderInfo.size()
                    //		+"=="+item.toString());
                    if (folderInfo.size() <= 0) {
                        LauncherModel.deleteItemFromDatabase(this, item);
						mDesktopItems.remove(item);
                        break;
                    }
                    switch ((int) item.container) {
                        case LauncherSettings.Favorites.CONTAINER_DESKTOP:{
                            final FolderIcon newFolder = FolderIcon.fromXml(
                                    R.layout.folder_icon, this, (ViewGroup) workspace
                                            .getChildAt(workspace.getCurrentScreen()),
                                    (UserFolderInfo) item);
                            checkAndAddNewLayout(item.screen);
							CellLayout layout = (CellLayout) workspace.getChildAt(item.screen);
                        	if(layout.isEmptyCell(item.cellX, item.cellY)){
                                workspace.addInScreen(newFolder, item.screen, item.cellX,
                                        item.cellY, 1, 1, false);
							} else {
//                        		Log.e(TAG, "bindItems(DESKTOP)==floder==no empty==x:"+item.cellX
//                        				+"==y:"+item.cellY+"==screen:"+item.screen);
                        		addOrUpdateAppInScreen(newFolder, folderInfo, false, 2);
                        	}
                            break;
						}
                        case LauncherSettings.Favorites.CONTAINER_NAVIGATEBAR: {
                            CellLayout layout = (CellLayout) navigetWorkspace.getChildAt(0);
                            int[] vacant = new int[2];
                            if (layout.getVacantCell(vacant, 1, 1)) {
                                final FolderIcon newNavigateFolder = FolderIcon
                                        .fromXml(R.layout.folder_icon, this,
                                                (ViewGroup) navigetWorkspace
                                                        .getChildAt(navigetWorkspace
                                                                .getCurrentScreen()),
                                                (UserFolderInfo) item);
                                navigetWorkspace.addInScreen(newNavigateFolder, 0, vacant[0],
                                        vacant[1], 1, 1, false);
                            }
                            break;
                        }
                        default:
                            break;
                    }
                    break;
                default:
                	break;
                }
                
               // Log.e(TAG, "bindItems()===end=="+item.toString());
            }
        }

        workspace.requestLayout();
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindFolders(HashMap<Long, FolderInfo> folders) {
        // mFolders.clear();
        // mFolders.putAll(folders);
    }

    /**
     * Add the views for a widget to the workspace. Implementation of the method
     * from LauncherModel.Callbacks.
     */
    // public void bindAppWidget(LauncherAppWidgetInfo item) {
    // final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
    // if (DEBUG_WIDGETS) {
    // Log.d(TAG, "bindAppWidget: " + item);
    // }
    // final Workspace workspace = mWorkspace;
    //
    // final int appWidgetId = item.appWidgetId;
    // final AppWidgetProviderInfo appWidgetInfo =
    // mAppWidgetManager.getAppWidgetInfo(appWidgetId);
    // if (DEBUG_WIDGETS) {
    // Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId +
    // " belongs to component "
    // + appWidgetInfo.provider);
    // }
    //
    // item.hostView = mAppWidgetHost.createView(this, appWidgetId,
    // appWidgetInfo);
    //
    // item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
    // item.hostView.setTag(item);
    //
    // workspace.addInScreen(item.hostView, item.screen, item.cellX, item.cellY,
    // item.spanX,
    // item.spanY, false);
    //
    // workspace.requestLayout();
    //
    // mDesktopItems.add(item);
    //
    // if (DEBUG_WIDGETS) {
    // Log.d(TAG, "bound widget id=" + item.appWidgetId + " in "
    // + (SystemClock.uptimeMillis() - start) + "ms");
    // }
    // }

    /**
     * Callback saying that there aren't any more items to bind. Implementation
     * of the method from LauncherModel.Callbacks.
     */
    public void finishBindingItems() {
    	setLoadOnResume();
    	
        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
            }

            final long[] userFolders = mSavedState.getLongArray(RUNTIME_STATE_USER_FOLDERS);
            if (userFolders != null) {
                // for (long folderId : userFolders) {
                // final FolderInfo info = mFolders.get(folderId);
                // if (info != null) {
                // openFolder(info);
                // }
                // }
                final Folder openFolder = getOpenIphoneFolder();
                if (openFolder != null) {
                    openFolder.requestFocus();
                }
            }

            mSavedState = null;
        }

        if (mSavedInstanceState != null) {
            super.onRestoreInstanceState(mSavedInstanceState);
            mSavedInstanceState = null;
        }

        mWorkspaceLoading = false;

        dismissHomeLoadingDialog();

        // to check new apps
        // mHandler.postDelayed(new Runnable() {
        // public void run() {
        // checkNewApps();
        // }
        // }, 10000);
    }
    
    private void addOrUpdateAppInScreen(View shortcut, ItemInfo info, boolean notify){
    	addOrUpdateAppInScreen(shortcut, info, notify, 1);
    }
    
    private void addOrUpdateAppInScreen(View shortcut, ItemInfo info, boolean notify, int startScreen){
    	boolean bGoted;
    	int nScreen = startScreen; 
    	if(nScreen >= mWorkspace.getChildCount()){
    		addNewLayout();
    	}
    	
    	do{
        	if(!(bGoted = addAppInScreen(nScreen, shortcut, info, notify))){
            	nScreen++;
            	if(nScreen >= mWorkspace.getChildCount()){
            		addNewLayout();
            	}
        	}
            
        }while(!bGoted);
    }
    
    private boolean addAppInScreen(int nScreen, View shortcut, ItemInfo info, boolean notify){
    	final CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(nScreen);
    	int[] vacant = new int[2];
    	
    	if (cellLayout.getVacantCell(vacant, 1, 1)) {
        	mWorkspace.addInScreen(shortcut, nScreen, vacant[0], vacant[1], 1, 1);
            // insert into database
            LauncherModel.addOrMoveItemInDatabase(this, info,
                    LauncherSettings.Favorites.CONTAINER_DESKTOP, nScreen, vacant[0],
                    vacant[1], notify);
            return true;
        }
    	return false;
    }
    
    private void addNewLayout(){
    	CellLayout layout = (CellLayout) mInflater.inflate(R.layout.workspace_screen, mWorkspace, false);
    	layout.setClickable(true);
        mWorkspace.addView(layout, -1);
        int nCount = mWorkspace.getChildCount();
        mIphoneIndicator.setCountAndIndex(nCount, mWorkspace.getCurrentScreen());
        
		// put into share preference
//		SharedPreferences sp = PreferenceManager
//		          .getDefaultSharedPreferences(this);
//		sp.edit().putInt(KEY_SCREEN_COUNT, nCount).commit();
    }
    
    private void checkAndAddNewLayout(int nItemScreen){
    	int nCount = nItemScreen+1 - mWorkspace.getChildCount();
    	if(nCount > 0){
	    	for(int i=0; i<nCount; i++){
	    		CellLayout layout = (CellLayout) mInflater.inflate(R.layout.workspace_screen, mWorkspace, false);
	        	layout.setClickable(true);
	            mWorkspace.addView(layout, -1);
	    	}
	    	
	    	nCount = mWorkspace.getChildCount();
	        mIphoneIndicator.setCountAndIndex(nCount, mWorkspace.getCurrentScreen());
	        
			// put into share preference
//			SharedPreferences sp = PreferenceManager
//			          .getDefaultSharedPreferences(this);
//			sp.edit().putInt(KEY_SCREEN_COUNT, nCount).commit();
    	}
    }
    
//    public void addItemToDesktop(ItemInfo info){
//    	android.util.Log.w("QsLog", "addItemToDesktop()======");
//    	if(info != null && mDesktopItems != null && !mDesktopItems.contains(mDesktopItems)){
//    		mDesktopItems.add(info);
//    	}
//    }
//    
//    public void removeItemFromDesktop(ItemInfo info){
//    	android.util.Log.w("QsLog", "removeItemFromDesktop()======");
//    	if(info != null && mDesktopItems != null/* && !mDesktopItems.contains(mDesktopItems)*/){
//    		mDesktopItems.remove(info);
//    	}
//    }
    
    private boolean isDesktopCached(ItemInfo info){
    	for(ItemInfo item : mDesktopItems){
    		
    		if(item.itemType == info.itemType){
		    	if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
		    			|| info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
		    		
		    		if(info.equals(item)){
		    			return true;
		    		}
		    	}/*else if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
		    		if(((ShortcutInfo)info).intent.getComponent().equals(((ShortcutInfo)item).intent.getComponent())){
		    			return true;
		    		}
		    	} */else if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER){
		    		if(item.id == info.id/* && ((FolderInfo)item).title.equals(((FolderInfo)info).title)*/)
		    			return true;
		    	}
    		}
	    }
    	return false;
    }

    /**
     * Add the icons for all apps. Implementation of the method from
     * LauncherModel.Callbacks.
     */
//    public void bindAllApplications(ArrayList<ApplicationInfo> apps) {
//        // mAllAppsGrid.setApps(apps);
//        final Workspace workspace = mWorkspace;
//        int count = workspace.getChildCount();
////Log.d(TAG, "bindAllApplications()====size:"+apps.size());
//        // remove all app or folder views in screen
//        for (int i = 1; i < count; i++) {
//            ((ViewGroup) workspace.getChildAt(i)).removeAllViewsInLayout();
//        }
//        ((ViewGroup) mNavigatebar.getChildAt(0)).removeAllViewsInLayout();
//        synchronized (LauncherModel.mObject) {
//            int appCount = apps.size();
//            boolean bGoted;
//            int nScreen = 1; 
//            // get the default navigate items
//            final ComponentName[] componentNames = Common.loadNavigates(this);
//            for (int i = 0; i < appCount; i++) {
//                final ApplicationInfo info = apps.get(i);
//                final ShortcutInfo shortcutInfo = info.makeShortcut();
//                
//                if(isDesktopCached(shortcutInfo)){
//                	Log.w(TAG, "bindAllApplications()===desktopcached=="+shortcutInfo.toString());
//                	continue;
//                }
//
//                View shortcut = createIphoneShortcut(shortcutInfo);
//                if (isInNavigate(componentNames, info)) {
//                    CellLayout layout = (CellLayout) mNavigatebar.getChildAt(0);
//                    int[] vacant = new int[2];
//                    if (layout.getVacantCell(vacant, 1, 1)) {
//                        mNavigatebar.addInScreen(shortcut, 0, vacant[0], vacant[1], 1, 1);
//                        // insert into database
//                        LauncherModel.addItemToDatabase(this, shortcutInfo,
//                                LauncherSettings.Favorites.CONTAINER_NAVIGATEBAR, 0, vacant[0],
//                                vacant[1], false);
//                    }
//                } else {
//                	
//                	do{
//                    	
//                    	if(!(bGoted = addAppInScreen(nScreen, shortcut, shortcutInfo))){
//                    		//QsLog.LogE("AllAppsWorkspace::setApps(0)==nScreen:"+nScreen+"==count:"+mWorkspace.getChildCount());
//        	            	nScreen++;
//        	            	if(nScreen >= mWorkspace.getChildCount()){
//        	            		addNewLayout();
//        	            	}
//        	            	//QsLog.LogE("AllAppsWorkspace::setApps(1)==nScreen:"+nScreen+"==count:"+mWorkspace.getChildCount());
//        	            	//cellLayout = (CellLayout) mWorkspace.getChildAt(nScreen);
//                    	}
//        	            
//                    }while(!bGoted);
//
//                }
//                // cache info
//                mDesktopItems.add(shortcutInfo);
//            }
//        }
//    }

    /**
     * A package was installed. Implementation of the method from
     * LauncherModel.Callbacks.
     */
    public void bindAppsAdded(ArrayList<ItemInfo> apps) {
    	setLoadOnResume();
        // removeDialog(DIALOG_CREATE_SHORTCUT);
        // mAllAppsGrid.addApps(apps);
        final Workspace workspace = mWorkspace;
        //int count = workspace.getChildCount();
        //int appCount = apps.size();
        int nScreen = 1; 
    	int nScreenCount = workspace.getChildCount();
    	Log.w(TAG, "bindAppsAdded()===appCount=="+apps.size());
    	for(ItemInfo item : apps){
        //for (int i = 0; i < appCount; i++) {

    		if(item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
	    			|| item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
    			
	            final AppShortcutInfo shortcutInfo = (AppShortcutInfo)item;
	            
	            //final ShortcutInfo shortcutInfo = info.makeShortcut();
	            if(isDesktopCached(shortcutInfo)){
	            	Log.w(TAG, "bindAppsAdded()===desktopcached=="+shortcutInfo.toString());
	            	continue;
	            }
	            
	            View shortcut = createIphoneShortcut(shortcutInfo);
	            
	            for(nScreen=1; nScreen<nScreenCount; nScreen++){
	            	if (addAppInScreen(nScreen, shortcut, shortcutInfo, false)) {
		                break;
		            }
	            }
	            //QsLog.LogE("AllAppsWorkspace::setApps(0)==nScreen:"+nScreen+"==count:"+nScreenCount);
	            if(nScreen >= nScreenCount){
	            	addNewLayout();
	            	nScreenCount = mWorkspace.getChildCount();
	            	addAppInScreen(nScreen, shortcut, shortcutInfo, false);
	            	//QsLog.LogE("AllAppsWorkspace::setApps(1)==nScreen:"+nScreen+"==count:"+mWorkspace.getChildCount());
	            }
	            
	            // cache info
	            mDesktopItems.add(shortcutInfo);
    		}
            
        }
    }

    /**
     * A package was updated. Implementation of the method from
     * LauncherModel.Callbacks.
     */
    public void bindAppsUpdated(ArrayList<ItemInfo> apps) {
    	setLoadOnResume();
    	Log.w(TAG, "bindAppsUpdated()==="+apps.size());
        // removeDialog(DIALOG_CREATE_SHORTCUT);
        mWorkspace.updateShortcuts(apps);
        mNavigatebar.updateShortcuts(apps);
        // mAllAppsGrid.updateApps(apps);
    }

    /**
     * A package was uninstalled. Implementation of the method from
     * LauncherModel.Callbacks.
     */
    //public void bindAppsRemoved(ArrayList<ApplicationInfo> apps) {
    public void bindAppsRemoved(ArrayList<ItemInfo> apps, boolean permanent){
    	
    	Log.w(TAG, "bindAppsRemoved()===size:"+apps.size()+"==permanent:"+permanent);
        mDesktopItems.removeAll(apps);
        // removeDialog(DIALOG_CREATE_SHORTCUT);
        mWorkspace.removeItems(apps);
        mNavigatebar.removeItems(apps);
        // mAllAppsGrid.removeApps(apps);
    }
    
    public void bindALostItem(ShortcutInfo info) {
        //final Workspace workspace = mWorkspace;
        //int count = workspace.getChildCount();
        Log.w(TAG, "bindALostItem(0)===="+info.toString());
        View shortcut = createIphoneShortcut(info);
        if(shortcut != null){
        	addOrUpdateAppInScreen(shortcut, info, true);
        	AnimManager.getInstance().startSingle(shortcut);
        	Log.w(TAG, "bindALostItem(1)===="+info.toString());
        }
//        if(isDesktopCached(info)){
//        	Log.w(TAG, "bindALostItem()===desktopcached=="+info.toString());
//        	return;
//        }
//        
//        // cache info
//        mDesktopItems.add(info);
//        for (int j = 1; j < count; j++) {
//            CellLayout cellLayout = (CellLayout) workspace.getChildAt(j);
//            int[] vacant = new int[2];
//            if (cellLayout.getVacantCell(vacant, 1, 1)) {
//                View shortcut = createIphoneShortcut(info);
//                workspace.addInScreen(shortcut, j, vacant[0], vacant[1], 1, 1);
//                AnimManager.getInstance().startSingle(shortcut);
//                // insert into database
//                LauncherModel.moveItemInDatabase(this, info,
//                        LauncherSettings.Favorites.CONTAINER_DESKTOP, j, vacant[0], vacant[1]);
//                break;
//            }
//            if (j == count - 1) {
//                // no more vacant for the add apps,we need create a new
//                // celllayout
//                CellLayout layout = (CellLayout) mInflater.inflate(R.layout.workspace_screen,
//                        mWorkspace, false);
//                mWorkspace.addView(layout, -1);
//                count += 1;
//                // put into share preference
////                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
////                sp.edit().putInt(KEY_SCREEN_COUNT, count).commit();
//            }
//        }
    }

    /**
     * Prints out out state for debugging.
     */
    public void dumpState() {
        Log.d(TAG, "BEGIN launcher2 dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
        Log.d(TAG, "mDesktopItems.size=" + mDesktopItems.size());
        // Log.d(TAG, "mFolders.size=" + mFolders.size());
        mModel.dumpState();
        // mAllAppsGrid.dumpState();
        Log.d(TAG, "END launcher2 dump state");
    }

    // public Indicator getIndicator() {
    // return mIndicator;
    // }

    // /////////////////////////////////////////////////////////////////////
    static final String MMSPACKAGENAME = "com.android.mms";

    static final String MMSCALSSNAME = "com.android.mms.ui.ConversationList";

    static final String PHONEPACKAGENAME = "com.android.contacts";

    static final String PHONECALSSNAME = "com.android.contacts.DialtactsActivity";

    static final int SMS_ICON_UPDATE_MSG = 1;

    static final int PHONE_ICON_UPDATE_MSG = 2;

    private final ContentObserver mSmsObserver = new SmsChangeObserver();

    private final ContentObserver mPhoneObserver = new PhoneChangeObserver();

    private void InitSmsAndPhoneObservers() {
        // modify by jz
        getContentResolver().registerContentObserver(Sms.CONTENT_URI, true, mSmsObserver);

        getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true,
                mPhoneObserver);
    }

    private void unInitSmsAndPhoneObserver() {
        // add by jz
        getContentResolver().unregisterContentObserver(mSmsObserver);

        getContentResolver().unregisterContentObserver(mPhoneObserver);
    }

    private class SmsChangeObserver extends ContentObserver {
        public SmsChangeObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            Log.d(TAG, "sms change");
            mShortcutUpdateHandler.sendEmptyMessage(SMS_ICON_UPDATE_MSG);
        }
    }

    private class PhoneChangeObserver extends ContentObserver {

        public PhoneChangeObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            Log.d(TAG, "phone change");
            mShortcutUpdateHandler.sendEmptyMessage(PHONE_ICON_UPDATE_MSG);
        }
    }

    private final Handler mShortcutUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SMS_ICON_UPDATE_MSG:
                    updateSmsIconEx();
                    break;
                case PHONE_ICON_UPDATE_MSG:
                    updatePhoneIconEx();
                    break;
                default:
                    Log.w("QiShang", "Unknown message: " + msg.what);
                    return;
            }
        }
    };

    /**
     * create mms icon with unread msg num on top left corner.add by zf -
     * QiShang
     * 
     * @param num TODO
     */

    public static Bitmap createUpdateIconWithNum(Bitmap icon, Context context, int num) {

        Bitmap numIcon = createNumIcon(num, context);

        int w = icon.getWidth();
        int h = icon.getHeight();

        Bitmap Ret_Icon = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(Ret_Icon);

        canvas.drawBitmap(icon, 0, 0, null);

        if (numIcon != null) {
            canvas.drawBitmap(numIcon, w - numIcon.getWidth(), 0, null);
        } else {
            Log.v("QiShang", "createMmsIconWithIcon()==numIcon is null==");
        }

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        return Ret_Icon;
    }

    static int[] numDrawable = {
            R.drawable.num_0, R.drawable.num_1, R.drawable.num_2, R.drawable.num_3,
            R.drawable.num_4, R.drawable.num_5, R.drawable.num_6, R.drawable.num_7,
            R.drawable.num_8, R.drawable.num_9
    };

    static Bitmap createNumIcon(int num, Context context) {
        // ignore num >= 100
        if (num <= 0 || num >= 100)
            return null;
        Bitmap bk = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.round))
                .getBitmap();
        int w = bk.getWidth();
        int h = bk.getHeight();
        Bitmap numIcon = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(numIcon);
        canvas.drawBitmap(bk, 0, 0, null);

        int tenPos = num / 10;
        int unitPos = num % 10;
        Bitmap tenBit = ((BitmapDrawable) context.getResources().getDrawable(numDrawable[tenPos]))
                .getBitmap();
        Bitmap unitBit = ((BitmapDrawable) context.getResources().getDrawable(numDrawable[unitPos]))
                .getBitmap();
        if (tenPos <= 0) {
            canvas.drawBitmap(unitBit, (w - unitBit.getWidth()) / 2, (h - unitBit.getHeight()) / 2,
                    null);
        } else {
            canvas.drawBitmap(tenBit, (w - tenBit.getWidth() - unitBit.getWidth()) / 2,
                    (h - tenBit.getHeight()) / 2, null);
            canvas.drawBitmap(unitBit,
                    (w - tenBit.getWidth() - unitBit.getWidth()) / 2 + tenBit.getWidth(),
                    (h - unitBit.getHeight()) / 2, null);
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return numIcon;
    }

    // jz
    private static final String NEW_INCOMING_SM_CONSTRAINT = "(" + Sms.TYPE + " = "
            + Sms.MESSAGE_TYPE_INBOX + " AND " + Sms.SEEN + " = 0)";

    // private static final String[] SMS_STATUS_PROJECTION = new String[] {
    // Sms.THREAD_ID
    // };

    static int getUnReadMsgCount(Context context) {

        int unReadMsgCount = 0;// Uri.parse("content://sms/inbox")
        Cursor cursor = context.getContentResolver().query(Sms.CONTENT_URI, null,
                NEW_INCOMING_SM_CONSTRAINT, null, null);

        if (cursor != null) {
            unReadMsgCount = cursor.getCount();
            cursor.close();
        }
        return unReadMsgCount;
    }

    static int getUnAcknowledgedMsgCount(Context context) {
        int UnAcknowledgedMsgCount = 0;
        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE + " AND "
                        + CallLog.Calls.NEW + " = " + 1, null, null);

        if (cursor != null) {
            UnAcknowledgedMsgCount = cursor.getCount();
            cursor.close();
        }
        return UnAcknowledgedMsgCount;
    }

    private void updateSmsIconEx() {
        if (mMmsAppView == null) {
            return;
        }
        int nCount = getUnReadMsgCount(this);
        AppShortcutInfo info = (AppShortcutInfo) mMmsAppView.getTag();
        final Bitmap oriIcon = info.getIcon(mIconCache);
        Bitmap updateIcon = oriIcon;
        if (nCount > 0) {
            updateIcon = createUpdateIconWithNum(oriIcon, this, nCount);

        }
        if (mMmsAppView instanceof BubbleTextView) {
            ((BubbleTextView) mMmsAppView).setIphoneIcon(updateIcon);
        }
    }

    private void updatePhoneIconEx() {
        if (mPhoneAppView == null) {
            return;
        }
        int nCount = getUnAcknowledgedMsgCount(this);
        AppShortcutInfo info = (AppShortcutInfo) mPhoneAppView.getTag();
        final Bitmap oriIcon = info.getIcon(mIconCache);
        Bitmap updateIcon = oriIcon;
        if (nCount > 0) {
            updateIcon = createUpdateIconWithNum(oriIcon, this, nCount);
        }
        if (mPhoneAppView instanceof BubbleTextView) {
            ((BubbleTextView) mPhoneAppView).setIphoneIcon(updateIcon);
        }
    }

    private void updateCalendarIcon() {
        if (mCalendarAppView == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        boolean isLongWeek = getResources().getBoolean(R.bool.config_long_week_text);
        final String week = DateUtils.getDayOfWeekString(calendar.get(Calendar.DAY_OF_WEEK),
                isLongWeek ? DateUtils.LENGTH_LONG : DateUtils.LENGTH_MEDIUM);
        final String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        AppShortcutInfo info = (AppShortcutInfo) mCalendarAppView.getTag();
        final Bitmap oriIcon = info.getIcon(mIconCache);
        int w = oriIcon.getWidth();
        int h = oriIcon.getHeight();
        Paint paint = new Paint();
        paint.setTextAlign(Align.LEFT);
        paint.setAntiAlias(true);
        Bitmap updateIcon = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(updateIcon);
        canvas.drawBitmap(oriIcon, 0, 0, null);
        paint.setTextSize(getResources().getDimensionPixelSize(
                R.dimen.iphone_calendar_week_textsize));
        paint.setColor(Color.WHITE);
        canvas.drawText(week, (w - paint.measureText(week)) / 2, paint.getTextSize(), paint);
        paint.setTextSize(getResources()
                .getDimensionPixelSize(R.dimen.iphone_calendar_day_textsize));
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.BLACK);
        canvas.drawText(day, (w - paint.measureText(day)) / 2, (h - paint.getTextSize()) / 2
                + paint.getTextSize(), paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        if (mCalendarAppView instanceof BubbleTextView) {
            ((BubbleTextView) mCalendarAppView).setIphoneIcon(updateIcon);
        }
    }

    private boolean isSmsApp(ComponentName name) {
        if (MMSPACKAGENAME.equals(name.getPackageName())
                && MMSCALSSNAME.equals(name.getClassName())) {
            return true;
        }
        return false;
    }

    private boolean isPhoneApp(ComponentName name) {
        if (PHONEPACKAGENAME.equals(name.getPackageName())
                && PHONECALSSNAME.equals(name.getClassName())) {
            return true;
        }
        return false;
    }

    private boolean isCalendarApp(ComponentName name) {
    	final String clsName = name.getClassName();
        if (clsName.equals("com.bluelotus.cncal.MainActivity")
                || clsName.equals("com.android.calendar.LaunchActivity")
                || clsName.equals("com.android.calendar.AllInOneActivity")) {
            return true;
        }
        
        
        return false;
    }

    // add by zf-qs
    private boolean isSystemApp(String packageName) {
        try {
            android.content.pm.ApplicationInfo info = mPm.getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if ((info.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            }
        } catch (NameNotFoundException e) {

        }
        return false;
    }

    private boolean isInNavigate(ComponentName[] infos, ApplicationInfo checkInfo) {
        for (ComponentName info : infos) {
            if (info.equals(checkInfo.componentName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGoogleMarketApp(ComponentName name) {
        if (name.getClassName().equals("com.android.vending.AssetBrowserActivity")) {
            return true;
        }
        return false;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    public Workspace getNavigatebar() {
        return mNavigatebar;
    }

    public DragController getDragController() {
        return mDragController;
    }

    public void handleIphoneFolderClick(FolderInfo folderInfo, View folderIcon) {
        Folder openFolder;
        if (folderInfo instanceof UserFolderInfo) {
            openFolder = UserFolder.fromXml(this);
        } else {
            return;
        }
        openFolder.setDragController(mDragController);
        openFolder.setLauncher(this);
        // mDragController.addDropTarget((UserFolder) openFolder);
        mDragLayer.addView(openFolder);
        openFolder.bind(folderInfo, folderIcon);
        openFolder.onOpen();
    }

    public boolean closeIphoneFolder() {
        final int N = mDragLayer.getChildCount();
        for (int i = 0; i < N; i++) {
            View view = mDragLayer.getChildAt(i);
            if (view instanceof UserFolder) {
                mDragLayer.removeView(view);
                ((UserFolder) view).onClose();
                return true;
            }
        }
        return false;
    }

    public UserFolder getOpenIphoneFolder() {
        final int N = mDragLayer.getChildCount();
        for (int i = 0; i < N; i++) {
            View view = mDragLayer.getChildAt(i);
            if (view instanceof UserFolder) {
                return (UserFolder) view;
            }
        }
        return null;
    }

    private View createInstallShortcut(AppShortcutInfo info) {
        final IphoneInstallAppShortCut installAppShortCut = (IphoneInstallAppShortCut) mInflater
                .inflate(R.layout.iphone_install_app_shortcut, null, false);
        installAppShortCut.setIphoneIcon(info.getIcon(mIconCache));
        installAppShortCut.setText(info.title);
        installAppShortCut.setTag(info);
        installAppShortCut.setOnClickListener(this);
        return installAppShortCut;
    }

    //public View createIphoneShortcut(ShortcutInfo info) {
    public View createIphoneShortcut(AppShortcutInfo info) {
        View view;
        ComponentName cn = info.getComponent();
//        if(info instanceof ApplicationInfo){
//        	cn = ((ApplicationInfo)info).componentName;
//        } else {
//        	cn = ((ShortcutInfo)info).intent.getComponent();
//        }
        //if (isSystemApp(cn.getPackageName())) {
        if(!info.isInstalledApp()){
            view = createShortcut(R.layout.application, null, info);
        } else {
            view = createInstallShortcut(info);
        }
        if (isSmsApp(cn)) {
            mMmsAppView = view;
            updateSmsIconEx();
        } else if (isPhoneApp(cn)) {
            mPhoneAppView = view;
            updatePhoneIconEx();
        } else if (isCalendarApp(cn)) {
            mCalendarAppView = view;
            updateCalendarIcon();
        }
        // else if
        // (com.mediatek.featureoption.FeatureOption.Qs_Framework_support_qsextstyle_iphone
        // && super.getQsUiStyle() == android.os.Build.QSUiSTYLE_IPHONE
        // && isGoogleMarketApp(info.intent.getComponent())) {
        // if (view instanceof IphoneInstallAppShortCut) {
        // ((IphoneInstallAppShortCut)
        // view).setText(getString(R.string.app_store));
        // } else if (view instanceof BubbleTextView) {
        // ((BubbleTextView) view).setText(R.string.app_store);
        // }
        // }

        return view;
    }

//    public View createIphoneTaskIcon(RunningTaskInfo info) {
//        final BubbleTextView shortcut = (BubbleTextView) mInflater.inflate(R.layout.application,
//                null);
//        ShortcutInfo shortcutInfo = mModel.getShortcutInfoFromTaskInfo(mPm, info, this);
//        if (shortcutInfo == null) {
//            return null;
//        }
//        shortcut.setIphoneIcon(shortcutInfo.getIcon(mIconCache));
//        shortcut.setText(shortcutInfo.title);
//        shortcut.setTag(shortcutInfo);
//        return shortcut;
//    }

    public void doUninstall(final ShortcutInfo info) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.delete_confirm_button_text,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Uri packageURI = Uri.parse("package:"
                                        + info.intent.getComponent().getPackageName());
                                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
                                        packageURI);
                                startActivity(uninstallIntent);
                                // PackageDeleteObserver observer = new
                                // PackageDeleteObserver();
                                // getPackageManager().deletePackage(
                                // info.intent.getComponent().getPackageName(),
                                // observer, 0);
                            }
                        }).setNegativeButton(R.string.cancel, null)
                .setMessage(String.format(getString(R.string.delete_notification), info.title))
                .create().show();
    }

  /*  class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(boolean succeeded) {
            Log.d(TAG, "uninstall " + succeeded);
        }
    }*/

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

//    public void bindALostItem(ShortcutInfo info) {
//        final Workspace workspace = mWorkspace;
//        int count = workspace.getChildCount();
//        
//        if(isDesktopCached(info)){
//        	Log.w(TAG, "bindALostItem()===desktopcached=="+info.toString());
//        	return;
//        }
//        
//        // cache info
//        mDesktopItems.add(info);
//        for (int j = 1; j < count; j++) {
//            CellLayout cellLayout = (CellLayout) workspace.getChildAt(j);
//            int[] vacant = new int[2];
//            if (cellLayout.getVacantCell(vacant, 1, 1)) {
//                View shortcut = createIphoneShortcut(info);
//                workspace.addInScreen(shortcut, j, vacant[0], vacant[1], 1, 1);
//                AnimManager.getInstance().startSingle(shortcut);
//                // insert into database
//                LauncherModel.moveItemInDatabase(this, info,
//                        LauncherSettings.Favorites.CONTAINER_DESKTOP, j, vacant[0], vacant[1]);
//                break;
//            }
//            if (j == count - 1) {
//                // no more vacant for the add apps,we need create a new
//                // celllayout
//                CellLayout layout = (CellLayout) mInflater.inflate(R.layout.workspace_screen,
//                        mWorkspace, false);
//                mWorkspace.addView(layout, -1);
//                count += 1;
//                // put into share preference
//                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//                sp.edit().putInt(KEY_SCREEN_COUNT, count).commit();
//            }
//        }
//    }
    
    final Runnable mReloadAppsRunnable = new Runnable() {
        @Override 
        public void run() {
        	resetViewsAndLoadApps();
        }
    };
    
    private void resetViewsAndLoadApps(){
    	Log.w(TAG, "resetViewsAndLoadApps()===mOrientation:"+mOrientation);
    	mDesktopItems.clear();
    	
    	//setupViews();
    	showHomeLoadingDialog();
    	
        if (mWorkspaceLoading) {
            mModel.setAllAppsDirty();
            mModel.startLoader(this, false);
        } else {
        	
        	
        	mModel.startLoader(this, false);
        	
            mHandler.postDelayed(new Runnable() {

                public void run() {
                	mWorkspace.moveToDefaultScreen(false);
                }
            }, 1000);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
    	Log.w(TAG, "onConfigurationChanged()===mOrientation:"+mOrientation+"==new:"+newConfig.orientation);
    	if(newConfig.orientation != mOrientation){
    		mOrientation = newConfig.orientation;
    		mHandler.removeCallbacks(mReloadAppsRunnable);
    		
    		if(mDragController != null)
    			mDragController.cancelDrag();
    		
    		//final int current_screen = mWorkspace.getCurrentScreen();
            setWallpaperDimension();
            closeIphoneFolder();
            mAnimManager.stop();
            
            if(mWorkspace.getCurrentScreen() == 0){
    	        final View v = getWindow().peekDecorView();
    	        if (v != null && v.getWindowToken() != null) {
    	            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    	            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    	        }
            }
            
            setupViews();
            
//            if(current_screen == 0)
//            	mWorkspace.moveToDefaultScreen(false);

            mHandler.postDelayed(mReloadAppsRunnable, 1000);
    	}

        super.onConfigurationChanged(newConfig);
    }

    public void showNavigatebar(boolean show) {
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            return;
        }
        if (mNavigateContainer != null) {
            if (show) {
                if (!mNavigateContainer.isShown()) {
                	if(mShowAnimation != null)
                		mNavigateContainer.startAnimation(mShowAnimation);
                	else
                		mNavigateContainer.setVisibility(View.VISIBLE);
                }
            } else {
                if (mNavigateContainer.isShown()) {
                	if(mHideAnimation != null)
                		mNavigateContainer.startAnimation(mHideAnimation);
                	else
                		mNavigateContainer.setVisibility(View.GONE);
                }
            } 

        }
    }

    private void initAnimation() {
        mHideAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mHideAnimation.setDuration(300);
        mHideAnimation.setInterpolator(new LinearInterpolator());
        mHideAnimation.setAnimationListener(mListener);

        mShowAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mShowAnimation.setDuration(300);
        mShowAnimation.setInterpolator(new LinearInterpolator());
        mShowAnimation.setAnimationListener(mListener);
    }

    private AnimationListener mListener = new AnimationListener() {
        public void onAnimationStart(Animation a) {
        }

        public void onAnimationRepeat(Animation a) {
        }

        public void onAnimationEnd(Animation a) {
            if (a == mShowAnimation) {
                mNavigateContainer.setVisibility(View.VISIBLE);
            } else if (a == mHideAnimation) {
                mNavigateContainer.setVisibility(View.GONE);
            }
        }
    };
    // private Handler mHandler = new Handler();
    //
    // private void checkNewApps() {
    // Log.d(TAG, "do new apps check");
    // // need to clear the applist's added apps;
    // AllAppsList appsList = mModel.getAllAppsList();
    // appsList.added = new ArrayList<ApplicationInfo>();
    // // check if has new app
    // final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    // mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    // final PackageManager packageManager = getPackageManager();
    // List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent,
    // 0);
    // Collections.sort(apps, new
    // ResolveInfo.DisplayNameComparator(packageManager));
    // for (ResolveInfo info : apps) {
    // appsList.add(new ApplicationInfo(info, mIconCache));
    // }
    // final ArrayList<ApplicationInfo> newApps = appsList.added;
    // appsList.added = new ArrayList<ApplicationInfo>();
    // for (ApplicationInfo info : newApps) {
    // Log.d(TAG, "New Apps is " + info.title);
    // }
    // bindAppsAdded(newApps);
    // }
}
