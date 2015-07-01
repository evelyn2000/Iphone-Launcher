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

import android.content.Context;
import android.util.EventLog;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Logs events to {@link EventLog}.
 */
public class EventLogLogger implements Logger {

    private static final char LIST_SEPARATOR = '|';

    private final Context mContext;

    private final Config mConfig;

    private final String mPackageName;

    private final Random mRandom;

    public EventLogLogger(Context context, Config config) {
        mContext = context;
        mConfig = config;
        mPackageName = mContext.getPackageName();
        mRandom = new Random();
    }

    protected Context getContext() {
        return mContext;
    }

    protected int getVersionCode() {
        return QsbApplication.get(getContext()).getVersionCode();
    }

    protected Config getConfig() {
        return mConfig;
    }

    public void logStart(int latency, String intentSource, Corpus corpus,
            List<Corpus> orderedCorpora) {
        // TODO: Add more info to startMethod
        String startMethod = intentSource;
        String currentCorpus = getCorpusLogName(corpus);
        String enabledCorpora = getCorpusLogNames(orderedCorpora);
        EventLogTags.writeQsbStart(mPackageName, getVersionCode(), startMethod,
                latency, currentCorpus, enabledCorpora, 0);
    }

    public void logSuggestionClick(int position, SuggestionCursor suggestionCursor,
            Collection<Corpus> queriedCorpora, int clickType) {
        String suggestions = getSuggestions(suggestionCursor);
        String corpora = getCorpusLogNames(queriedCorpora);
        int numChars = suggestionCursor.getUserQuery().length();
        EventLogTags.writeQsbClick(position, suggestions, corpora, numChars,
                clickType);
    }

    public void logSearch(Corpus corpus, int startMethod, int numChars) {
        String corpusName = getCorpusLogName(corpus);
        EventLogTags.writeQsbSearch(corpusName, startMethod, numChars);
    }

    public void logVoiceSearch(Corpus corpus) {
        String corpusName = getCorpusLogName(corpus);
        EventLogTags.writeQsbVoiceSearch(corpusName);
    }

    public void logExit(SuggestionCursor suggestionCursor, int numChars) {
        String suggestions = getSuggestions(suggestionCursor);
        EventLogTags.writeQsbExit(suggestions, numChars);
    }

    public void logLatency(CorpusResult result) {
        if (!shouldLogLatency()) return;
        String corpusName = getCorpusLogName(result.getCorpus());
        int latency = result.getLatency();
        int numChars = result.getUserQuery().length();
        EventLogTags.writeQsbLatency(corpusName, latency, numChars);
    }

    private boolean shouldLogLatency() {
        int freq = mConfig.getLatencyLogFrequency();
        return freq > mRandom.nextInt(1000);
    }

    private String getCorpusLogName(Corpus corpus) {
        if (corpus == null) return null;
        return corpus.getName();
    }

    private String getSuggestions(SuggestionCursor cursor) {
        StringBuilder sb = new StringBuilder();
        final int count = cursor == null ? 0 : cursor.getCount();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(LIST_SEPARATOR);
            cursor.moveTo(i);
            String source = cursor.getSuggestionSource().getName();
            String type = cursor.getSuggestionLogType();
            if (type == null) type = "";
            String shortcut = cursor.isSuggestionShortcut() ? "shortcut" : "";
            sb.append(source).append(':').append(type).append(':').append(shortcut);
        }
        return sb.toString();
    }

    private String getCorpusLogNames(Collection<Corpus> corpora) {
        StringBuilder sb = new StringBuilder();
        for (Corpus corpus : corpora) {
            if (sb.length() > 0) sb.append(LIST_SEPARATOR);
            sb.append(getCorpusLogName(corpus));
        }
        return sb.toString();
    }

}
