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

package com.android.quicksearchbox.util;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * Executor that uses a single thread and an unbounded work queue.
 */
public class SingleThreadNamedTaskExecutor implements NamedTaskExecutor {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.SingleThreadNamedTaskExecutor";

    private final LinkedBlockingQueue<NamedTask> mQueue;
    private final Thread mWorker;
    private volatile boolean mClosed = false;

    public SingleThreadNamedTaskExecutor(ThreadFactory threadFactory) {
        mQueue = new LinkedBlockingQueue<NamedTask>();
        mWorker = threadFactory.newThread(new Worker());
        mWorker.start();
    }

    public void cancelPendingTasks() {
        if (DBG) Log.d(TAG, "Cancelling " + mQueue.size() + " tasks: " + mWorker.getName());
        if (mClosed) {
            throw new IllegalStateException("cancelPendingTasks() after close()");
        }
        mQueue.clear();
    }

    public void close() {
        mClosed = true;
        mWorker.interrupt();
        mQueue.clear();
    }

    public void execute(NamedTask task) {
        if (mClosed) {
            throw new IllegalStateException("execute() after close()");
        }
        mQueue.add(task);
    }

    private class Worker implements Runnable {
        public void run() {
            try {
                loop();
            } finally {
                if (!mClosed) Log.w(TAG, "Worker exited before close");
            }
        }

        private void loop() {
            Thread currentThread = Thread.currentThread();
            String threadName = currentThread.getName();
            while (!mClosed) {
                NamedTask task;
                try {
                    task = mQueue.take();
                } catch (InterruptedException ex) {
                    continue;
                }
                currentThread.setName(threadName + " " + task.getName());
                try {
                    task.run();
                } catch (RuntimeException ex) {
                    Log.e(TAG, "Task " + task.getName() + " failed", ex);
                }
            }
        }
    }

    public static Factory<NamedTaskExecutor> factory(final ThreadFactory threadFactory) {
        return new Factory<NamedTaskExecutor>() {
            public NamedTaskExecutor create() {
                return new SingleThreadNamedTaskExecutor(threadFactory);
            }
        };
    }

}
