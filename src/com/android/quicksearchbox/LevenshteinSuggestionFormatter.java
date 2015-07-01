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

import com.android.quicksearchbox.util.LevenshteinDistance;
import com.android.quicksearchbox.util.LevenshteinDistance.Token;
import com.google.common.annotations.VisibleForTesting;

import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;

/**
 * Suggestion formatter using the Levenshtein distance (minumum edit distance) to calculate the
 * formatting.
 */
public class LevenshteinSuggestionFormatter extends SuggestionFormatter {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.LevenshteinSuggestionFormatter";

    public LevenshteinSuggestionFormatter(TextAppearanceFactory spanFactory) {
        super(spanFactory);
    }

    @Override
    public Spanned formatSuggestion(String query, String suggestion) {
        if (DBG) Log.d(TAG, "formatSuggestion('" + query + "', '" + suggestion + "')");
        query = normalizeQuery(query);
        final Token[] queryTokens = tokenize(query);
        final Token[] suggestionTokens = tokenize(suggestion);
        final int[] matches = findMatches(queryTokens, suggestionTokens);
        if (DBG){
            Log.d(TAG, "source = " + queryTokens);
            Log.d(TAG, "target = " + suggestionTokens);
            Log.d(TAG, "matches = " + matches);
        }
        final SpannableString str = new SpannableString(suggestion);

        final int matchesLen = matches.length;
        for (int i = 0; i < matchesLen; ++i) {
            final Token t = suggestionTokens[i];
            int sourceLen = 0;
            int thisMatch = matches[i];
            if (thisMatch >= 0) {
                sourceLen = queryTokens[thisMatch].length();
            }
            applySuggestedTextStyle(str, t.mStart + sourceLen, t.mEnd);
            applyQueryTextStyle(str, t.mStart, t.mStart + sourceLen);
        }

        return str;
    }

    private String normalizeQuery(String query) {
        return query.toLowerCase();
    }

    /**
     * Finds which tokens in the target match tokens in the source.
     *
     * @param source List of source tokens (i.e. user query)
     * @param target List of target tokens (i.e. suggestion)
     * @return The indices into source which target tokens correspond to. A non-negative value n at
     *      position i means that target token i matches source token n. A negative value means that
     *      the target token i does not match any source token.
     */
    @VisibleForTesting
    int[] findMatches(Token[] source, Token[] target) {
        final LevenshteinDistance table = new LevenshteinDistance(source, target);
        table.calculate();
        final int targetLen = target.length;
        final int[] result = new int[targetLen];
        LevenshteinDistance.EditOperation[] ops = table.getTargetOperations();
        for (int i = 0; i < targetLen; ++i) {
            if (ops[i].getType() == LevenshteinDistance.EDIT_UNCHANGED) {
                result[i] = ops[i].getPosition();
            } else {
                result[i] = -1;
            }
        }
        return result;
    }

    @VisibleForTesting
    Token[] tokenize(final String seq) {
        int pos = 0;
        final int len = seq.length();
        final char[] chars = seq.toCharArray();
        // There can't be more tokens than characters, make an array that is large enough
        Token[] tokens = new Token[len];
        int tokenCount = 0;
        while (pos < len) {
            while (pos < len && (chars[pos] == ' ' || chars[pos] == '\t')) {
                pos++;
            }
            int start = pos;
            while (pos < len && !(chars[pos] == ' ' || chars[pos] == '\t')) {
                pos++;
            }
            int end = pos;
            if (start != end) {
                tokens[tokenCount++] = new Token(chars, start, end);
            }
        }
        // Create a token array of the right size and return
        Token[] ret = new Token[tokenCount];
        System.arraycopy(tokens, 0, ret, 0, tokenCount);
        return ret;
    }

}
