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

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A consumer that consumes a fixed number of values. When the expected number of values
 * has been consumed, further values are rejected.
 */
public class BarrierConsumer<A> implements Consumer<A> {

    private final Lock mLock = new ReentrantLock();
    private final Condition mNotFull = mLock.newCondition();

    private final int mExpectedCount;

    // Set to null when getValues() returns.
    private ArrayList<A> mValues;

    /**
     * Constructs a new BarrierConsumer.
     *
     * @param expectedCount The number of values to consume.
     */
    public BarrierConsumer(int expectedCount) {
        mExpectedCount = expectedCount;
        mValues = new ArrayList<A>(expectedCount);
    }

    /**
     * Blocks until the expected number of results is available, or until the thread is
     * interrupted. This method should not be called multiple times.
     *
     * @return A list of values, never {@code null}.
     */
    public ArrayList<A> getValues() {
        mLock.lock();
        try {
            try {
                while (!isFull()) {
                    mNotFull.await();
                }
            } catch (InterruptedException ex) {
                // Return the values that we've gotten so far
            }
            ArrayList<A> values = mValues;
            mValues = null;  // mark that getValues() has returned
            return values;
        } finally {
            mLock.unlock();
        }
    }

    public boolean consume(A value) {
        mLock.lock();
        try {
            // Do nothing if getValues() has alrady returned,
            // or enough values have already been consumed
            if (mValues == null || isFull()) {
                return false;
            }
            mValues.add(value);
            if (isFull()) {
                // Wake up any thread waiting in getValues()
                mNotFull.signal();
            }
            return true;
        } finally {
            mLock.unlock();
        }
    }

    private boolean isFull() {
        return mValues.size() == mExpectedCount;
    }
}
