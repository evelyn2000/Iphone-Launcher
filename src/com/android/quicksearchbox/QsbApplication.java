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
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.quicksearchbox;

import com.android.iphonelauncher.LauncherApplication;
import com.android.quicksearchbox.google.GoogleSource;
import com.android.quicksearchbox.google.GoogleSuggestClient;
import com.android.quicksearchbox.ui.DelayingSuggestionsAdapter;
import com.android.quicksearchbox.ui.SuggestionViewFactory;
import com.android.quicksearchbox.ui.SuggestionViewInflater;
import com.android.quicksearchbox.ui.SuggestionsAdapter;
import com.android.quicksearchbox.util.Factory;
import com.android.quicksearchbox.util.NamedTaskExecutor;
import com.android.quicksearchbox.util.PerNameExecutor;
import com.android.quicksearchbox.util.PriorityThreadFactory;
import com.android.quicksearchbox.util.SingleThreadNamedTaskExecutor;
//import com.google.common.util.concurrent.NamingThreadFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

//import android.app.SearchEngineInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.mediatek.common.search.SearchEngineInfo;
import com.mediatek.common.search.ISearchEngineManager;
import android.app.SearchManager;

public class QsbApplication {

    private final Context mContext;

    private int mVersionCode;
    private Handler mUiThreadHandler;
    private Config mConfig;
    private Corpora mCorpora;
    private CorpusRanker mCorpusRanker;
    private ShortcutRepository mShortcutRepository;
    private ShortcutRefresher mShortcutRefresher;
    private NamedTaskExecutor mSourceTaskExecutor;
    private ThreadFactory mQueryThreadFactory;
    private SuggestionsProvider mSuggestionsProvider;
    private SuggestionViewFactory mSuggestionViewFactory;
//    private CorpusViewFactory mCorpusViewFactory;
    private GoogleSource mGoogleSource;
    private VoiceSearch mVoiceSearch;
    private Logger mLogger;
    private SuggestionFormatter mSuggestionFormatter;
    private TextAppearanceFactory mTextAppearanceFactory;
    
    private SearchEngineInfo mSearchEngineInfo;
    private final static String TAG = "QsbApplication";
    private final static boolean DBG = true;

    public QsbApplication(Context context) {
        mContext = context;
    }
    
    public void setSearchEngineInfo(String name) {
        try {
        	mSearchEngineInfo = null;

            ISearchEngineManager searchEngineManager = (ISearchEngineManager) mContext
                    .getSystemService(Context.SEARCH_ENGINE_SERVICE);
            
            mSearchEngineInfo = searchEngineManager.getBestMatchSearchEngine("", name) != null
                    ? searchEngineManager.getBestMatchSearchEngine("", name)
                    : searchEngineManager.getDefaultSearchEngine();
            
        	//mSearchEngineInfo = SearchEngineInfo.getSpecifiedSearchEngineInfo(mContext, name);
        } catch (IllegalArgumentException exception) {
            Log.e(TAG, "Cannot load search engine " + name, exception);
        }
    }
    
    public SearchEngineInfo getSearchEngineInfo() {
    	if (mSearchEngineInfo == null) {
    		createSearchEngineInfo();
    	}
    	return mSearchEngineInfo;
    }
    
    public void createSearchEngineInfo() {
        SharedPreferences p =
                //PreferenceManager.getDefaultSharedPreferences(mContext);
        mContext.getSharedPreferences(SearchSettings.PREFERENCES_NAME, Context.MODE_PRIVATE);
        updateSearchEngine(p);
    }
    
    public static final String GOOGLE = "google";
    public static final String PREF_SEARCH_ENGINE = "search_engine";
    
    
	public void updateSearchEngine(SharedPreferences p) {
		String searchEngineName = p.getString(PREF_SEARCH_ENGINE, GOOGLE);
		if (mSearchEngineInfo == null || !mSearchEngineInfo.getName().equals(searchEngineName)) {
			setSearchEngineInfo(searchEngineName);
		}
		if (DBG) {
			Log.i(TAG, "Selected search engine: " + mSearchEngineInfo.getName());
			Log.i(TAG, getSearchEngineInfo().getName());
		}
		//broadcastSearchEngineChanged();
	}
	
	public void updateSearchEngineExternal(SharedPreferences p, String newSearchEngine) {
		if (newSearchEngine == null) {
			return;
		}
		if (mSearchEngineInfo == null || !mSearchEngineInfo.getName().equals(newSearchEngine)) {
			setSearchEngineInfo(newSearchEngine);
			SharedPreferences.Editor editor = p.edit();
			editor.putString(PREF_SEARCH_ENGINE, newSearchEngine);
			editor.commit();
		}
		if (DBG) {
			Log.i(TAG, "Selected search engine: " + mSearchEngineInfo.getName());
			Log.i(TAG, getSearchEngineInfo().getName());
		}
		//broadcastSearchEngineChanged();
	}
	
	/*private void broadcastSearchEngineChanged() {
		// We use a message broadcast since the listeners could be in multiple
		// processes.
		Intent intent = new Intent(
				SearchWidgetProvider.ACTION_SEARCH_ENGINE_CHANGED);
		Log.i(TAG, "Broadcasting: " + intent);
		mContext.sendBroadcast(intent);
	}*/

    public static boolean isFroyoOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static QsbApplication get(Context context) {
        return ((LauncherApplication) context.getApplicationContext()).getApp();
    }

    protected Context getContext() {
        return mContext;
    }

    public int getVersionCode() {
        if (mVersionCode == 0) {
            try {
                PackageManager pm = getContext().getPackageManager();
                PackageInfo pkgInfo = pm.getPackageInfo(getContext().getPackageName(), 0);
                mVersionCode = pkgInfo.versionCode;
            } catch (PackageManager.NameNotFoundException ex) {
                // The current package should always exist, how else could we
                // run code from it?
                throw new RuntimeException(ex);
            }
        }
        return mVersionCode;
    }

    protected void checkThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("Accessed Application object from thread "
                    + Thread.currentThread().getName());
        }
    }

    public void close() {
        checkThread();
        if (mConfig != null) {
            mConfig.close();
            mConfig = null;
        }
        if (mShortcutRepository != null) {
            mShortcutRepository.close();
            mShortcutRepository = null;
        }
        if (mSourceTaskExecutor != null) {
            mSourceTaskExecutor.close();
            mSourceTaskExecutor = null;
        }
        if (mSuggestionsProvider != null) {
            mSuggestionsProvider.close();
            mSuggestionsProvider = null;
        }
    }

    public synchronized Handler getMainThreadHandler() {
        if (mUiThreadHandler == null) {
            mUiThreadHandler = new Handler(Looper.getMainLooper());
        }
        return mUiThreadHandler;
    }

    public void runOnUiThread(Runnable action) {
        getMainThreadHandler().post(action);
    }

    /**
     * Indicates that construction of the QSB UI is now complete.
     */
    public void onStartupComplete() {
    }

    /**
     * Gets the QSB configuration object.
     * May be called from any thread.
     */
    public synchronized Config getConfig() {
        if (mConfig == null) {
            mConfig = createConfig();
        }
        return mConfig;
    }

    protected Config createConfig() {
        return new Config(getContext());
    }

    /**
     * Gets the corpora.
     * May only be called from the main thread.
     */
    public Corpora getCorpora() {
        checkThread();
        if (mCorpora == null) {
            mCorpora = createCorpora();
        }
        return mCorpora;
    }

    protected Corpora createCorpora() {
        SearchableCorpora corpora = new SearchableCorpora(getContext(), createSources(),
                createCorpusFactory());
        corpora.update();
        return corpora;
    }

    /**
     * Updates the corpora, if they are loaded.
     * May only be called from the main thread.
     */
    public void updateCorpora() {
        checkThread();
        if (mCorpora != null) {
            mCorpora.update();
        }
    }

    protected Sources createSources() {
        return new SearchableSources(getContext());
    }

    protected CorpusFactory createCorpusFactory() {
        int numWebCorpusThreads = getConfig().getNumWebCorpusThreads();
        return new SearchableCorpusFactory(getContext(), getConfig(),
                createExecutorFactory(numWebCorpusThreads));
    }

    protected Factory<Executor> createExecutorFactory(final int numThreads) {
        final ThreadFactory threadFactory = getQueryThreadFactory();
        return new Factory<Executor>() {
            public Executor create() {
                return Executors.newFixedThreadPool(numThreads, threadFactory);
            }
        };
    }

    /**
     * Gets the corpus ranker.
     * May only be called from the main thread.
     */
    public CorpusRanker getCorpusRanker() {
        checkThread();
        if (mCorpusRanker == null) {
            mCorpusRanker = createCorpusRanker();
        }
        return mCorpusRanker;
    }

    protected CorpusRanker createCorpusRanker() {
        return new DefaultCorpusRanker(getCorpora(), getShortcutRepository());
    }

    /**
     * Gets the shortcut repository.
     * May only be called from the main thread.
     */
    public ShortcutRepository getShortcutRepository() {
        checkThread();
        if (mShortcutRepository == null) {
            mShortcutRepository = createShortcutRepository();
        }
        return mShortcutRepository;
    }

    protected ShortcutRepository createShortcutRepository() {
        ThreadFactory logThreadFactory =
                new ThreadFactoryBuilder()
                .setNameFormat("ShortcutRepository #%d")
                .setThreadFactory(new PriorityThreadFactory(
                        Process.THREAD_PRIORITY_BACKGROUND))
                .build();
        Executor logExecutor = Executors.newSingleThreadExecutor(logThreadFactory);
        return ShortcutRepositoryImplLog.create(getContext(), getConfig(), getCorpora(),
            getShortcutRefresher(), getMainThreadHandler(), logExecutor);
    }

    /**
     * Gets the shortcut refresher.
     * May only be called from the main thread.
     */
    public ShortcutRefresher getShortcutRefresher() {
        checkThread();
        if (mShortcutRefresher == null) {
            mShortcutRefresher = createShortcutRefresher();
        }
        return mShortcutRefresher;
    }

    protected ShortcutRefresher createShortcutRefresher() {
        // For now, ShortcutRefresher gets its own SourceTaskExecutor
        return new SourceShortcutRefresher(createSourceTaskExecutor());
    }

    /**
     * Gets the source task executor.
     * May only be called from the main thread.
     */
    public NamedTaskExecutor getSourceTaskExecutor() {
        checkThread();
        if (mSourceTaskExecutor == null) {
            mSourceTaskExecutor = createSourceTaskExecutor();
        }
        return mSourceTaskExecutor;
    }

    protected NamedTaskExecutor createSourceTaskExecutor() {
        ThreadFactory queryThreadFactory = getQueryThreadFactory();
        return new PerNameExecutor(SingleThreadNamedTaskExecutor.factory(queryThreadFactory));
    }

    /**
     * Gets the query thread factory.
     * May only be called from the main thread.
     */
    protected ThreadFactory getQueryThreadFactory() {
        checkThread();
        if (mQueryThreadFactory == null) {
            mQueryThreadFactory = createQueryThreadFactory();
        }
        return mQueryThreadFactory;
    }

    protected ThreadFactory createQueryThreadFactory() {
        String nameFormat = "QSB #%d";
        int priority = getConfig().getQueryThreadPriority();
        return new ThreadFactoryBuilder()
                .setNameFormat(nameFormat)
                .setThreadFactory(new PriorityThreadFactory(priority))
                .build();
    }

    /**
     * Gets the suggestion provider.
     * May only be called from the main thread.
     */
    public SuggestionsProvider getSuggestionsProvider() {
        checkThread();
        if (mSuggestionsProvider == null) {
            mSuggestionsProvider = createSuggestionsProvider();
        }
        return mSuggestionsProvider;
    }

    protected SuggestionsProvider createSuggestionsProvider() {
        int maxShortcutsPerWebSource = getConfig().getMaxShortcutsPerWebSource();
        int maxShortcutsPerNonWebSource = getConfig().getMaxShortcutsPerNonWebSource();
        Promoter allPromoter = new ShortcutLimitingPromoter(
                maxShortcutsPerWebSource,
                maxShortcutsPerNonWebSource,
                new ShortcutPromoter(
                        new RankAwarePromoter(getConfig(), getCorpora())));
        Promoter singleCorpusPromoter = new ShortcutPromoter(new ConcatPromoter());
        SuggestionsProviderImpl provider = new SuggestionsProviderImpl(getConfig(),
                getSourceTaskExecutor(),
                getMainThreadHandler(),
                getShortcutRepository(),
                getCorpora(),
                getCorpusRanker(),
                getLogger());
        provider.setAllPromoter(allPromoter);
        provider.setSingleCorpusPromoter(singleCorpusPromoter);
        return provider;
    }

    /**
     * Gets the suggestion view factory.
     * May only be called from the main thread.
     */
    public SuggestionViewFactory getSuggestionViewFactory() {
        checkThread();
        if (mSuggestionViewFactory == null) {
            mSuggestionViewFactory = createSuggestionViewFactory();
        }
        return mSuggestionViewFactory;
    }

    protected SuggestionViewFactory createSuggestionViewFactory() {
        return new SuggestionViewInflater(getContext());
    }

    /**
     * Gets the corpus view factory.
     * May only be called from the main thread.
     */
//    public CorpusViewFactory getCorpusViewFactory() {
//        checkThread();
//        if (mCorpusViewFactory == null) {
//            mCorpusViewFactory = createCorpusViewFactory();
//        }
//        return mCorpusViewFactory;
//    }
//
//    protected CorpusViewFactory createCorpusViewFactory() {
//        return new CorpusViewInflater(getContext());
//    }

    /**
     * Creates a suggestions adapter.
     * May only be called from the main thread.
     */
    public SuggestionsAdapter createSuggestionsAdapter() {
        SuggestionViewFactory viewFactory = getSuggestionViewFactory();
        DelayingSuggestionsAdapter adapter = new DelayingSuggestionsAdapter(viewFactory);
        return adapter;
    }

    /**
     * Gets the Google source.
     * May only be called from the main thread.
     */
    public GoogleSource getGoogleSource() {
        checkThread();
        if (mGoogleSource == null) {
            mGoogleSource = createGoogleSource();
        }
        return mGoogleSource;
    }

    protected GoogleSource createGoogleSource() {
        return new GoogleSuggestClient(getContext());
    }

    /**
     * Gets Voice Search utilities.
     */
    public VoiceSearch getVoiceSearch() {
        checkThread();
        if (mVoiceSearch == null) {
            mVoiceSearch = createVoiceSearch();
        }
        return mVoiceSearch;
    }

    protected VoiceSearch createVoiceSearch() {
        return new VoiceSearch(getContext());
    }

    /**
     * Gets the event logger.
     * May only be called from the main thread.
     */
    public Logger getLogger() {
        checkThread();
        if (mLogger == null) {
            mLogger = createLogger();
        }
        return mLogger;
    }

    protected Logger createLogger() {
        return new EventLogLogger(getContext(), getConfig());
    }

    public SuggestionFormatter getSuggestionFormatter() {
        if (mSuggestionFormatter == null) {
            mSuggestionFormatter = createSuggestionFormatter();
        }
        return mSuggestionFormatter;
    }

    protected SuggestionFormatter createSuggestionFormatter() {
        return new LevenshteinSuggestionFormatter(getTextAppearanceFactory());
    }

    public TextAppearanceFactory getTextAppearanceFactory() {
        if (mTextAppearanceFactory == null) {
            mTextAppearanceFactory = createTextAppearanceFactory();
        }
        return mTextAppearanceFactory;
    }

    protected TextAppearanceFactory createTextAppearanceFactory() {
        return new TextAppearanceFactory(getContext());
    }
}
