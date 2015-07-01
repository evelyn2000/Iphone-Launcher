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


import com.android.quicksearchbox.util.BarrierConsumer;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Base class for corpora backed by multiple sources.
 */
public abstract class MultiSourceCorpus extends AbstractCorpus {

    private final Executor mExecutor;

    private final ArrayList<Source> mSources;

    // calculated values based on properties of sources:
    private boolean mSourcePropertiesValid;
    private int mQueryThreshold;
    private boolean mQueryAfterZeroResults;
    private boolean mVoiceSearchEnabled;
    private boolean mIsLocationAware;

    public MultiSourceCorpus(Context context, Config config,
            Executor executor, Source... sources) {
        super(context, config);
        mExecutor = executor;

        mSources = new ArrayList<Source>();
        for (Source source : sources) {
            addSource(source);
        }

    }

    protected void addSource(Source source) {
        if (source != null) {
            mSources.add(source);
            // invalidate calculated values:
            mSourcePropertiesValid = false;
        }
    }

    public Collection<Source> getSources() {
        return mSources;
    }

    /**
     * Creates a corpus result object for a set of source results.
     * This method should not call {@link Result#fill}.
     *
     * @param query The query text.
     * @param results The results of the queries.
     * @param latency Latency in milliseconds of the suggestion queries.
     * @return An instance of {@link Result} or a subclass of it.
     */
    protected Result createResult(String query, ArrayList<SourceResult> results, int latency) {
        return new Result(query, results, latency);
    }

    /**
     * Gets the sources to query for suggestions for the given input.
     *
     * @param query The current input.
     * @param onlyCorpus If true, this is the only corpus being queried.
     * @return The sources to query.
     */
    protected List<Source> getSourcesToQuery(String query, boolean onlyCorpus) {
        List<Source> sources = new ArrayList<Source>();
        for (Source candidate : getSources()) {
            if (candidate.getQueryThreshold() <= query.length()) {
                sources.add(candidate);
            }
        }
        return sources;
    }

    private void updateSourceProperties() {
        if (mSourcePropertiesValid) return;
        mQueryThreshold = Integer.MAX_VALUE;
        mQueryAfterZeroResults = false;
        mVoiceSearchEnabled = false;
        mIsLocationAware = false;
        for (Source s : getSources()) {
            mQueryThreshold = Math.min(mQueryThreshold, s.getQueryThreshold());
            mQueryAfterZeroResults |= s.queryAfterZeroResults();
            mVoiceSearchEnabled |= s.voiceSearchEnabled();
            mIsLocationAware |= s.isLocationAware();
        }
        if (mQueryThreshold == Integer.MAX_VALUE) {
            mQueryThreshold = 0;
        }
        mSourcePropertiesValid = true;
    }

    public int getQueryThreshold() {
        updateSourceProperties();
        return mQueryThreshold;
    }

    public boolean queryAfterZeroResults() {
        updateSourceProperties();
        return mQueryAfterZeroResults;
    }

    public boolean voiceSearchEnabled() {
        updateSourceProperties();
        return mVoiceSearchEnabled;
    }

    public boolean isLocationAware() {
        updateSourceProperties();
        return mIsLocationAware;
    }

    public CorpusResult getSuggestions(String query, int queryLimit, boolean onlyCorpus) {
        LatencyTracker latencyTracker = new LatencyTracker();
        List<Source> sources = getSourcesToQuery(query, onlyCorpus);
        BarrierConsumer<SourceResult> consumer =
                new BarrierConsumer<SourceResult>(sources.size());
        boolean onlySource = sources.size() == 1;
        for (Source source : sources) {
            QueryTask<SourceResult> task = new QueryTask<SourceResult>(query, queryLimit,
                    source, null, consumer, onlySource);
            mExecutor.execute(task);
        }
        ArrayList<SourceResult> results = consumer.getValues();
        int latency = latencyTracker.getLatency();
        Result result = createResult(query, results, latency);
        result.fill();
        return result;
    }

    /**
     * Base class for results returned by {@link MultiSourceCorpus#getSuggestions}.
     * Subclasses of {@link MultiSourceCorpus} should override
     * {@link MultiSourceCorpus#createResult} and return an instance of this class or a
     * subclass.
     */
    protected class Result extends ListSuggestionCursor implements CorpusResult {

        private final ArrayList<SourceResult> mResults;

        private final int mLatency;

        public Result(String userQuery, ArrayList<SourceResult> results, int latency) {
            super(userQuery);
            mResults = results;
            mLatency = latency;
        }

        protected ArrayList<SourceResult> getResults() {
            return mResults;
        }

        /**
         * Fills the list of suggestions using the list of results.
         * The default implementation concatenates the results.
         */
        public void fill() {
            for (SourceResult result : getResults()) {
                int count = result.getCount();
                for (int i = 0; i < count; i++) {
                    result.moveTo(i);
                    add(new SuggestionPosition(result));
                }
            }
        }

        public Corpus getCorpus() {
            return MultiSourceCorpus.this;
        }

        public int getLatency() {
            return mLatency;
        }

        @Override
        public void close() {
            super.close();
            for (SourceResult result : mResults) {
                result.close();
            }
        }

        @Override
        public String toString() {
            return getCorpus() + "[" + getUserQuery() + "]";
        }
    }

}
