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
import com.android.quicksearchbox.util.Util;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.webkit.URLUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * The web search source.
 */
public class WebCorpus extends MultiSourceCorpus {

    private static final String WEB_CORPUS_NAME = "web";

    private final Source mWebSearchSource;

    private final Source mBrowserSource;

    public WebCorpus(Context context, Config config, Executor executor,
            Source webSearchSource, Source browserSource) {
        super(context, config, executor, webSearchSource, browserSource);
        mWebSearchSource = webSearchSource;
        mBrowserSource = browserSource;
    }

    public CharSequence getLabel() {
        return getContext().getText(R.string.corpus_label_web);
    }

    public CharSequence getHint() {
        // The web corpus uses a drawable hint instead
        return null;
    }

    private boolean isUrl(String query) {
       return Patterns.WEB_URL.matcher(query).matches();
    }

    public Intent createSearchIntent(String query, Bundle appData) {
        if (isUrl(query)) {
            return createBrowseIntent(query);
        } else if (mWebSearchSource != null){
            return mWebSearchSource.createSearchIntent(query, appData);
        } else {
            return null;
        }
    }

    private Intent createBrowseIntent(String query) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String url = URLUtil.guessUrl(query);
        intent.setData(Uri.parse(url));
        return intent;
    }

    public SuggestionData createSearchShortcut(String query) {
        SuggestionData shortcut = new SuggestionData(mWebSearchSource);
        if (isUrl(query)) {
            shortcut.setIntentAction(Intent.ACTION_VIEW);
            shortcut.setIcon1(String.valueOf(R.drawable.globe));
            shortcut.setText1(query);
            // Set query so that trackball selection works
            shortcut.setSuggestionQuery(query);
            shortcut.setIntentData(URLUtil.guessUrl(query));
        } else {
            shortcut.setIntentAction(Intent.ACTION_WEB_SEARCH);
            shortcut.setIcon1(String.valueOf(R.drawable.magnifying_glass));
            shortcut.setText1(query);
            shortcut.setSuggestionQuery(query);
        }
        return shortcut;
    }

    public Intent createVoiceSearchIntent(Bundle appData) {
        if (mWebSearchSource != null){
            return mWebSearchSource.createVoiceSearchIntent(appData);
        } else {
            return null;
        }
    }

    private int getCorpusIconResource() {
        return R.drawable.corpus_icon_web;
    }

    public Drawable getCorpusIcon() {
        return getContext().getResources().getDrawable(getCorpusIconResource());
    }

    public Uri getCorpusIconUri() {
        return Util.getResourceUri(getContext(), getCorpusIconResource());
    }

    public String getName() {
        return WEB_CORPUS_NAME;
    }

    @Override
    public int getQueryThreshold() {
        return 0;
    }

    @Override
    public boolean queryAfterZeroResults() {
        return true;
    }

    @Override
    public boolean voiceSearchEnabled() {
        return true;
    }

    public boolean isWebCorpus() {
        return true;
    }

    public CharSequence getSettingsDescription() {
        return getContext().getText(R.string.corpus_description_web);
    }

    @Override
    protected List<Source> getSourcesToQuery(String query, boolean onlyCorpus) {
        ArrayList<Source> sourcesToQuery = new ArrayList<Source>(2);
        if (mWebSearchSource != null
                && SearchSettings.getShowWebSuggestions(getContext())) {
            sourcesToQuery.add(mWebSearchSource);
        }
        if (mBrowserSource != null && query.length() > 0) {
            sourcesToQuery.add(mBrowserSource);
        }
        return sourcesToQuery;
    }

    @Override
    protected Result createResult(String query, ArrayList<SourceResult> results, int latency) {
        return new WebResult(query, results, latency);
    }

    protected class WebResult extends Result {

        public WebResult(String query, ArrayList<SourceResult> results, int latency) {
            super(query, results, latency);
        }

        @Override
        public void fill() {
            SourceResult webSearchResult = null;
            SourceResult browserResult = null;
            for (SourceResult result : getResults()) {
                if (result.getSource().equals(mWebSearchSource)) {
                    webSearchResult = result;
                } else {
                    browserResult = result;
                }
            }
            if (browserResult != null && browserResult.getCount() > 0) {
                add(new SuggestionPosition(browserResult, 0));
            }
            if (webSearchResult != null) {
                int count = webSearchResult.getCount();
                for (int i = 0; i < count; i++) {
                    add(new SuggestionPosition(webSearchResult, i));
                }
            }
        }

    }
}
