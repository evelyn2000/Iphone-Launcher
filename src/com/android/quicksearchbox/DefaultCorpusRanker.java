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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DefaultCorpusRanker extends AbstractCorpusRanker {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.DefaultCorpusRanker";

    private final ShortcutRepository mShortcuts;

    public DefaultCorpusRanker(Corpora corpora, ShortcutRepository shortcuts) {
        super(corpora);
        mShortcuts = shortcuts;
    }

    private static class CorpusComparator implements Comparator<Corpus> {
        private final Map<String,Integer> mClickScores;

        public CorpusComparator(Map<String,Integer> clickScores) {
            mClickScores = clickScores;
        }

        public int compare(Corpus corpus1, Corpus corpus2) {
            boolean corpus1IsDefault = corpus1.isCorpusDefaultEnabled();
            boolean corpus2IsDefault = corpus2.isCorpusDefaultEnabled();

            if (corpus1IsDefault != corpus2IsDefault) {
                // Default corpora always come before non-default
                return corpus1IsDefault ? -1 : 1;
            } else {
                // Then by descending score
                return getCorpusScore(corpus2) - getCorpusScore(corpus1);
            }
        }

        /**
         * Scores a corpus. Higher score is better.
         */
        private int getCorpusScore(Corpus corpus) {
            // Web corpus always comes first
            if (corpus.isWebCorpus()) {
                return Integer.MAX_VALUE;
            }
            // Then use click score
            Integer clickScore = mClickScores.get(corpus.getName());
            if (clickScore != null) {
                return clickScore;
            }
            return 0;
        }
    }

    @Override
    public List<Corpus> rankCorpora(Corpora corpora) {
        Collection<Corpus> enabledCorpora = corpora.getEnabledCorpora();
        if (DBG) Log.d(TAG, "Ranking: " + enabledCorpora);

        Map<String,Integer> clickScores = mShortcuts.getCorpusScores();
        ArrayList<Corpus> ordered = new ArrayList<Corpus>(enabledCorpora);
        Collections.sort(ordered, new CorpusComparator(clickScores));

        if (DBG) Log.d(TAG, "Click scores: " + clickScores);
        if (DBG) Log.d(TAG, "Ordered: " + ordered);
        return ordered;
    }

}
