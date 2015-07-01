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

package com.android.quicksearchbox.util;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes NamedTasks in batches of a given size.  Tasks are queued until
 * executeNextBatch is called.
 */
public class BatchingNamedTaskExecutor implements NamedTaskExecutor {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.BatchingNamedTaskExecutor";

    private final NamedTaskExecutor mExecutor;

    /** Queue of tasks waiting to be dispatched to mExecutor **/
    private final ArrayList<NamedTask> mQueuedTasks = new ArrayList<NamedTask>();

    /**
     * Creates a new BatchingSourceTaskExecutor.
     *
     * @param executor A SourceTaskExecutor for actually executing the tasks.
     */
    public BatchingNamedTaskExecutor(NamedTaskExecutor executor) {
        mExecutor = executor;
    }

    public void execute(NamedTask task) {
        synchronized (mQueuedTasks) {
            if (DBG) Log.d(TAG, "Queuing " + task);
            mQueuedTasks.add(task);
        }
    }

    private void dispatch(NamedTask task) {
        if (DBG) Log.d(TAG, "Dispatching " + task);
        mExecutor.execute(task);
    }

    /**
     * Instructs the executor to submit the next batch of results.
     * @param batchSize the maximum number of entries to execute.
     */
    public void executeNextBatch(int batchSize) {
        NamedTask[] batch = new NamedTask[0];
        synchronized (mQueuedTasks) {
            int count = Math.min(mQueuedTasks.size(), batchSize);
            List<NamedTask> nextTasks = mQueuedTasks.subList(0, count);
            batch = nextTasks.toArray(batch);
            nextTasks.clear();
            if (DBG) Log.d(TAG, "Dispatching batch of " + count);
        }

        for (NamedTask task : batch) {
            dispatch(task);
        }
    }

    /**
     * Cancel any unstarted tasks running in this executor.  This instance 
     * should not be re-used after calling this method.
     */
    public void cancelPendingTasks() {
        synchronized (mQueuedTasks) {
            mQueuedTasks.clear();
        }
        mExecutor.cancelPendingTasks();
    }

    public void close() {
        cancelPendingTasks();
        mExecutor.close();
    }
}
