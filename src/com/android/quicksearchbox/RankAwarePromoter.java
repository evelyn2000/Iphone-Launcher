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

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A promoter that gives preference to suggestions from higher ranking corpora.
 */
public class RankAwarePromoter implements Promoter {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.RankAwarePromoter";

    private final Config mConfig;
    private final Corpora mCorpora;

    public RankAwarePromoter(Config config, Corpora corpora) {
        mConfig = config;
        mCorpora = corpora;
    }

    public void pickPromoted(SuggestionCursor shortcuts, ArrayList<CorpusResult> suggestions,
            int maxPromoted, ListSuggestionCursor promoted) {

        if (DBG) Log.d(TAG, "Available results: " + suggestions);

        // Split non-empty results into default sources and other, positioned at first suggestion
        LinkedList<CorpusResult> defaultResults = new LinkedList<CorpusResult>();
        LinkedList<CorpusResult> otherResults = new LinkedList<CorpusResult>();
        for (CorpusResult result : suggestions) {
            if (result.getCount() > 0) {
                result.moveTo(0);
                Corpus corpus = result.getCorpus();
                if (corpus == null || corpus.isCorpusDefaultEnabled()) {
                    defaultResults.add(result);
                } else {
                    otherResults.add(result);
                }
            }
        }

        // Share the top slots equally among each of the default corpora
        if (maxPromoted > 0 && !defaultResults.isEmpty()) {
            int slotsToFill = Math.min(getSlotsAboveKeyboard() - promoted.getCount(), maxPromoted);
            if (slotsToFill > 0) {
                int stripeSize = Math.max(1, slotsToFill / defaultResults.size());
                maxPromoted -= roundRobin(defaultResults, slotsToFill, stripeSize, promoted);
            }
        }

        // Then try to fill with the remaining promoted results
        if (maxPromoted > 0 && !defaultResults.isEmpty()) {
            int stripeSize = Math.max(1, maxPromoted / defaultResults.size());
            maxPromoted -= roundRobin(defaultResults, maxPromoted, stripeSize, promoted);
            // We may still have a few slots left
            maxPromoted -= roundRobin(defaultResults, maxPromoted, maxPromoted, promoted);
        }

        // Then try to fill with the rest
        if (maxPromoted > 0 && !otherResults.isEmpty()) {
            int stripeSize = Math.max(1, maxPromoted / otherResults.size());
            maxPromoted -= roundRobin(otherResults, maxPromoted, stripeSize, promoted);
            // We may still have a few slots left
            maxPromoted -= roundRobin(otherResults, maxPromoted, maxPromoted, promoted);
        }

        if (DBG) Log.d(TAG, "Returning " + promoted.toString());
    }

    private int getSlotsAboveKeyboard() {
        return mConfig.getNumSuggestionsAboveKeyboard();
    }

    /**
     * Promotes "stripes" of suggestions from each corpus.
     *
     * @param results     the list of CorpusResults from which to promote.
     *                    Exhausted CorpusResults are removed from the list.
     * @param maxPromoted maximum number of suggestions to promote.
     * @param stripeSize  number of suggestions to take from each corpus.
     * @param promoted    the list to which promoted suggestions are added.
     * @return the number of suggestions actually promoted.
     */
    private int roundRobin(LinkedList<CorpusResult> results, int maxPromoted, int stripeSize,
            ListSuggestionCursor promoted) {
        int count = 0;
        if (maxPromoted > 0 && !results.isEmpty()) {
            for (Iterator<CorpusResult> iter = results.iterator();
                 count < maxPromoted && iter.hasNext();) {
                CorpusResult result = iter.next();
                count += promote(result, stripeSize, promoted);
                if (result.getPosition() == result.getCount()) {
                    iter.remove();
                }
            }
        }
        return count;
    }

    /**
     * Copies suggestions from a SuggestionCursor to the list of promoted suggestions.
     *
     * @param cursor from which to copy the suggestions
     * @param count maximum number of suggestions to copy
     * @param promoted the list to which to add the suggestions
     * @return the number of suggestions actually copied.
     */
    private int promote(SuggestionCursor cursor, int count, ListSuggestionCursor promoted) {
        if (count < 1 || cursor.getPosition() >= cursor.getCount()) {
            return 0;
        }
        int i = 0;
        do {
            promoted.add(new SuggestionPosition(cursor));
            i++;
        } while (cursor.moveToNext() && i < count);
        return i;
    }
}
