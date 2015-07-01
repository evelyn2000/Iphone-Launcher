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

package com.android.iphonelauncher;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

/**
 * Provides an animation between 0.0f and 1.0f over a given duration.
 */
class SymmetricalLinearTween {

    private static final int FPS = 30;
    private static final int FRAME_TIME = 1000 / FPS;

    Handler mHandler;
    int mDuration;
    TweenCallback mCallback;

    boolean mRunning;
    long mBase;
    boolean mDirection;
    float mValue;

    /**
     * @param duration milliseconds duration
     * @param callback callbacks
     */
    public SymmetricalLinearTween(boolean initial, int duration, TweenCallback callback) {
        mValue = initial ? 1.0f : 0.0f;
        mDirection = initial;
        mDuration = duration;
        mCallback = callback;
        mHandler = new Handler();
    }

    /**
     * Starts the tweening.
     *
     * @param direction If direction is true, the value goes towards 1.0f.  If direction
     *                  is false, the value goes towards 0.0f.
     */
    public void start(boolean direction) {
        start(direction, SystemClock.uptimeMillis());
    }

    /**
     * Starts the tweening.
     *
     * @param direction If direction is true, the value goes towards 1.0f.  If direction
     *                  is false, the value goes towards 0.0f.
     * @param baseTime  The time to use as zero for this animation, in the
     *                  {@link SystemClock.uptimeMillis} time base.  This allows you to
     *                  synchronize multiple animations.
     */
    public void start(boolean direction, long baseTime) {
        if (direction != mDirection) {
            if (!mRunning) {
                mBase = baseTime;
                mRunning = true;
                mCallback.onTweenStarted();
                long next = SystemClock.uptimeMillis() + FRAME_TIME;
                mHandler.postAtTime(mTick, next);
            } else {
                // reverse direction
                long now = SystemClock.uptimeMillis();
                long diff = now - mBase;
                mBase = now + diff - mDuration;
            }
            mDirection = direction;
        }
    }

    Runnable mTick = new Runnable() {
        public void run() {
            long base = mBase;
            long now = SystemClock.uptimeMillis();
            long diff = now-base;
            int duration = mDuration;
            float val = diff/(float)duration;
            if (!mDirection) {
                val = 1.0f - val;
            }
            if (val > 1.0f) {
                val = 1.0f;
            } else if (val < 0.0f) {
                val = 0.0f;
            }
            float old = mValue;
            mValue = val;
            mCallback.onTweenValueChanged(val, old);
            int frame = (int)(diff / FRAME_TIME);
            long next = base + ((frame+1)*FRAME_TIME);
            if (diff < duration) {
                mHandler.postAtTime(this, next);
            }
            if (diff >= duration) {
                mCallback.onTweenFinished();
                mRunning = false;
            }
        }
    };
}

