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

import com.google.common.annotations.VisibleForTesting;

import android.os.Handler;
import android.util.Log;

import java.util.HashSet;

/**
 * A SuggestionCursor that allows shortcuts to be updated by overlaying
 * with results from another cursor.
 */
class ShortcutCursor extends ListSuggestionCursor {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.ShortcutCursor";

    // mShortcuts is used to close the underlying cursor when we're closed.
    private final SuggestionCursor mShortcuts;
    // mRefreshed contains all the cursors that have been refreshed, so that
    // they can be closed when ShortcutCursor is closed.
    private final HashSet<SuggestionCursor> mRefreshed;

    private final ShortcutRefresher mRefresher;
    private final ShortcutRepository mShortcutRepo;
    private final Handler mUiThread;

    private boolean mClosed;

    private ShortcutCursor(String query, SuggestionCursor shortcuts, Handler uiThread,
            ShortcutRefresher refresher, ShortcutRepository repository) {
        super(query);
        mShortcuts = shortcuts;
        mUiThread = uiThread;
        mRefresher = refresher;
        mShortcutRepo = repository;
        mRefreshed = new HashSet<SuggestionCursor>();
    }

    @VisibleForTesting
    public ShortcutCursor(String query, Handler uiThread,
            ShortcutRefresher refresher, ShortcutRepository repository) {
        this(query, null, uiThread, refresher, repository);
    }

    public ShortcutCursor(SuggestionCursor suggestions, Handler uiThread,
            ShortcutRefresher refresher, ShortcutRepository repository) {
        this(suggestions.getUserQuery(), suggestions, uiThread, refresher, repository);
        if (suggestions == null) return;
        int count = suggestions.getCount();
        if (DBG) Log.d(TAG, "Total shortcuts: " + count);
        for (int i = 0; i < count; i++) {
            suggestions.moveTo(i);
            if (suggestions.getSuggestionSource() != null) {
                add(new SuggestionPosition(suggestions));
            } else {
                if (DBG) Log.d(TAG, "Skipping shortcut " + i);
            }
        }
    }

    /**
     * Refresh a shortcut from this cursor.
     *
     * @param shortcut The shortcut to refresh. Should be a shortcut taken from this cursor.
     */
    public void refresh(Suggestion shortcut) {
        mRefresher.refresh(shortcut, new ShortcutRefresher.Listener() {
            public void onShortcutRefreshed(final Source source,
                    final String shortcutId, final SuggestionCursor refreshed) {
                if (DBG) Log.d(TAG, "Shortcut refreshed: " + shortcutId);
                mShortcutRepo.updateShortcut(source, shortcutId, refreshed);
                mUiThread.post(new Runnable() {
                    public void run() {
                        refresh(source, shortcutId, refreshed);
                    }
                });
            }
        });
    }

    /**
     * Updates this SuggestionCursor with a refreshed result from another.
     * Since this modifies the cursor, it should be called on the UI thread.
     * This class assumes responsibility for closing refreshed.
     */
    private void refresh(Source source, String shortcutId, SuggestionCursor refreshed) {
        if (DBG) Log.d(TAG, "refresh " + shortcutId);
        if (mClosed) {
            if (refreshed != null) {
                refreshed.close();
            }
            return;
        }
        if (refreshed != null) {
            mRefreshed.add(refreshed);
        }
        for (int i = 0; i < getCount(); i++) {
            moveTo(i);
            if (shortcutId.equals(getShortcutId()) && source.equals(getSuggestionSource())) {
                if (refreshed != null && refreshed.getCount() > 0) {
                    if (DBG) Log.d(TAG, "replacing row " + i);
                    replaceRow(new SuggestionPosition(refreshed));
                } else {
                    if (DBG) Log.d(TAG, "removing row " + i);
                    removeRow();
                }
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void close() {
        if (DBG) Log.d(TAG, "close()");
        if (mClosed) {
            throw new IllegalStateException("Double close()");
        }
        super.close();
        mClosed = true;
        if (mShortcuts != null) {
            mShortcuts.close();
        }
        for (SuggestionCursor cursor : mRefreshed) {
             cursor.close();
        }
        super.close();
    }
}