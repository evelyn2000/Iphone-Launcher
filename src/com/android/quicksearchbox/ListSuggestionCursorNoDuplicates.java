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

import android.util.Log;

import java.util.HashSet;

/**
 * A SuggestionCursor that is backed by a list of SuggestionPosition objects
 * and doesn't allow duplicate suggestions.
 */
public class ListSuggestionCursorNoDuplicates extends ListSuggestionCursor {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.ListSuggestionCursorNoDuplicates";

    private final HashSet<String> mSuggestionKeys;

    public ListSuggestionCursorNoDuplicates(String userQuery) {
        super(userQuery);
        mSuggestionKeys = new HashSet<String>();
    }

    @Override
    public boolean add(Suggestion suggestion) {
        String key = getSuggestionKey(suggestion);
        if (mSuggestionKeys.add(key)) {
            return super.add(suggestion);
        } else {
            if (DBG) Log.d(TAG, "Rejecting duplicate " + key);
            return false;
        }
    }

    /**
     * Gets a unique key that identifies a suggestion. This is used to avoid
     * duplicate suggestions in the promoted list.
     */
    private String getSuggestionKey(Suggestion suggestion) {
        String action = makeKeyComponent(suggestion.getSuggestionIntentAction());
        String data = makeKeyComponent(normalizeUrl(suggestion.getSuggestionIntentDataString()));
        String query = makeKeyComponent(normalizeUrl(suggestion.getSuggestionQuery()));
        // calculating accurate size of string builder avoids an allocation vs starting with
        // the default size and having to expand.
        int size = action.length() + 2 + data.length() + query.length();
        return new StringBuilder(size)
                .append(action)
                .append('#')
                .append(data)
                .append('#')
                .append(query)
                .toString();
    }

    private String makeKeyComponent(String str) {
        return str == null ? "" : str;
    }

    /** Simple url normalization that strips http:// and empty paths, i.e.,
     *  http://www.google.com/ -> www.google.com.  Used to prevent obvious
     * duplication of nav suggestions, bookmarks and urls entered by the user.
     */
    private static String normalizeUrl(String url) {
        if (url != null && url.startsWith("http://")) {
            int start = 7;   // length of http://
            int end = url.length();
            if (url.indexOf('/', start) == end - 1) {
                end--;
            }
            return url.substring(start, end);
        }
        return url;
    }

}
