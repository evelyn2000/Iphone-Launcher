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

import java.util.Collection;
import java.util.List;


/**
 * Interface for logging implementations.
 */
public interface Logger {

    public static final int SEARCH_METHOD_BUTTON = 0;
    public static final int SEARCH_METHOD_KEYBOARD = 1;

    public static final int SUGGESTION_CLICK_TYPE_LAUNCH = 0;
    public static final int SUGGESTION_CLICK_TYPE_REFINE = 1;
    public static final int SUGGESTION_CLICK_TYPE_QUICK_CONTACT = 2;

    /**
     * Called when QSB has started.
     *
     * @param latency User-visible start-up latency in milliseconds.
     */
    void logStart(int latency, String intentSource, Corpus corpus,
            List<Corpus> orderedCorpora);

    /**
     * Called when a suggestion is clicked.
     *
     * @param position 0-based position of the suggestion in the UI.
     * @param suggestionCursor all the suggestions shown in the UI.
     * @param queriedCorpora all corpora that were queried to produce the suggestions in
     *        {@code suggestionCursor}, ordered by rank.
     * @param clickType One of the SUGGESTION_CLICK_TYPE constants.
     */
    void logSuggestionClick(int position, SuggestionCursor suggestionCursor,
            Collection<Corpus> queriedCorpora, int clickType);

    /**
     * The user launched a search.
     *
     * @param startMethod One of {@link #SEARCH_METHOD_BUTTON} or {@link #SEARCH_METHOD_KEYBOARD}.
     * @param numChars The number of characters in the query.
     */
    void logSearch(Corpus corpus, int startMethod, int numChars);

    /**
     * The user launched a voice search.
     */
    void logVoiceSearch(Corpus corpus);

    /**
     * The user left QSB without performing any action (click suggestions, search or voice search).
     *
     * @param suggestionCursor all the suggestions shown in the UI when the user left
     * @param numChars The number of characters in the query typed when the user left.
     */
    void logExit(SuggestionCursor suggestionCursor, int numChars);

    /**
     * Logs the latency of a suggestion query to a specific source.
     *
     * @param result The result of the query.
     */
    void logLatency(CorpusResult result);

}
