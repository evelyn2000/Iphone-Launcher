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

import com.android.quicksearchbox.util.Consumer;
import com.android.quicksearchbox.util.NamedTask;
import com.android.quicksearchbox.util.NamedTaskExecutor;

import android.os.Handler;

/**
 * A task that gets suggestions from a corpus.
 */
public class QueryTask<C extends SuggestionCursor> implements NamedTask {
    private final String mQuery;
    private final int mQueryLimit;
    private final SuggestionCursorProvider<C> mProvider;
    private final Handler mHandler;
    private final Consumer<C> mConsumer;
    private final boolean mTheOnlyOne;

    /**
     * Creates a new query task.
     *
     * @param query Query to run.
     * @param queryLimit The number of suggestions to ask each provider for.
     * @param provider The provider to ask for suggestions.
     * @param handler Handler that {@link Consumer#consume} will
     *        get called on. If null, the method is called on the query thread.
     * @param consumer Consumer to notify when the suggestions have been returned.
     * @param onlyTask Indicates if this is the only task within a batch.
     */
    public QueryTask(String query, int queryLimit, SuggestionCursorProvider<C> provider,
            Handler handler, Consumer<C> consumer, boolean onlyTask) {
        mQuery = query;
        mQueryLimit = queryLimit;
        mProvider = provider;
        mHandler = handler;
        mConsumer = consumer;
        mTheOnlyOne = onlyTask;
    }

    public String getName() {
        return mProvider.getName();
    }

    public void run() {
        final C cursor = mProvider.getSuggestions(mQuery, mQueryLimit, mTheOnlyOne);
        if (mHandler == null) {
            mConsumer.consume(cursor);
        } else {
            mHandler.post(new Runnable() {
                public void run() {
                    boolean accepted = mConsumer.consume(cursor);
                    if (!accepted) {
                        cursor.close();
                    }
                }
            });
        }
    }

    @Override
    public String toString() {
        return mProvider + "[" + mQuery + "]";
    }

    public static <C extends SuggestionCursor> void startQueries(String query,
            int maxResultsPerProvider,
            Iterable<? extends SuggestionCursorProvider<C>> providers,
            NamedTaskExecutor executor, Handler handler,
            Consumer<C> consumer, boolean onlyOneProvider) {

        for (SuggestionCursorProvider<C> provider : providers) {
            QueryTask<C> task = new QueryTask<C>(query, maxResultsPerProvider, provider, handler,
                    consumer, onlyOneProvider);
            executor.execute(task);
        }
    }

}
