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
 */

/*
 * Copyright (C) 2010 The Android Open Source Project
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

import com.android.iphonelauncher.R;
import com.android.quicksearchbox.util.Factory;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Executor;

/**
 * Creates corpora.
 */
public class SearchableCorpusFactory implements CorpusFactory {

    private static final String TAG = "QSB.SearchableCorpusFactory";

    private final Context mContext;

    private final Config mConfig;

    private final Factory<Executor> mWebCorpusExecutorFactory;

    public SearchableCorpusFactory(Context context, Config config,
            Factory<Executor> webCorpusExecutorFactory) {
        mContext = context;
        mConfig = config;
        mWebCorpusExecutorFactory = webCorpusExecutorFactory;
    }

    public Collection<Corpus> createCorpora(Sources sources) {
        ArrayList<Corpus> corpora = new ArrayList<Corpus>();
        addSpecialCorpora(corpora, sources);
        addSingleSourceCorpora(corpora, sources);
        return corpora;
    }

    protected Context getContext() {
        return mContext;
    }

    protected Config getConfig() {
        return mConfig;
    }

    protected Executor createWebCorpusExecutor() {
        return mWebCorpusExecutorFactory.create();
    }

    /**
     * Adds any corpora that are not simple single source corpora.
     *
     * @param corpora List to add corpora to.
     * @param sources All available sources.
     */
    protected void addSpecialCorpora(ArrayList<Corpus> corpora, Sources sources) {
        addCorpus(corpora, createWebCorpus(sources));
        addCorpus(corpora, createAppsCorpus(sources));
    }

    /**
     * Adds corpora for all sources that are not already used by a corpus.
     *
     * @param corpora List to add the new corpora to. Corpora will not be created for the sources
     *        used by corpora already in this list.
     * @param sources Sources to create corpora for.
     */
    protected void addSingleSourceCorpora(ArrayList<Corpus> corpora, Sources sources) {
        // Set of all sources that are already used
        HashSet<Source> claimedSources = new HashSet<Source>();
        for (Corpus specialCorpus : corpora) {
            claimedSources.addAll(specialCorpus.getSources());
        }

        // Creates corpora for all unclaimed sources
        for (Source source : sources.getSources()) {
            if (!claimedSources.contains(source)) {
                addCorpus(corpora, createSingleSourceCorpus(source));
            }
        }
    }

    private void addCorpus(ArrayList<Corpus> corpora, Corpus corpus) {
        if (corpus != null) corpora.add(corpus);
    }

    protected Corpus createWebCorpus(Sources sources) {
        Source webSource = sources.getWebSearchSource();
        if (webSource != null && !webSource.canRead()) {
            Log.w(TAG, "Can't read web source " + webSource.getName());
            webSource = null;
        }
        Source browserSource = getBrowserSource(sources);
        if (browserSource != null && !browserSource.canRead()) {
            Log.w(TAG, "Can't read browser source " + browserSource.getName());
            browserSource = null;
        }
        Executor executor = createWebCorpusExecutor();
        return new WebCorpus(mContext, mConfig, executor, webSource, browserSource);
    }

    protected Corpus createAppsCorpus(Sources sources) {
        Source appsSource = getAppsSource(sources);
        return new AppsCorpus(mContext, mConfig, appsSource);
    }

    protected Corpus createSingleSourceCorpus(Source source) {
        if (!source.canRead()) return null;
        return new SingleSourceCorpus(mContext, mConfig, source);
    }

    protected Source getBrowserSource(Sources sources) {
        String name = getContext().getString(R.string.browser_search_component);
        return sources.getSource(name);
    }

    protected Source getAppsSource(Sources sources) {
        String name = getContext().getString(R.string.installed_apps_component);
        return sources.getSource(name);
    }

}
