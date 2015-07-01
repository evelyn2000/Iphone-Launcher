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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import java.util.Collection;

/**
 * A corpus is a user-visible set of suggestions. A corpus gets suggestions from one
 * or more sources.
 *
 * Objects that implement this interface should override {@link Object#equals(Object)}
 * and {@link Object#hashCode()} so that they can be used as keys in hash maps.
 */
public interface Corpus extends SuggestionCursorProvider<CorpusResult> {

    /**
     * Gets the localized, human-readable label for this corpus.
     */
    CharSequence getLabel();

    /**
     * Gets the icon for this corpus.
     */
    Drawable getCorpusIcon();

    /**
     * Gets the icon URI for this corpus.
     */
    Uri getCorpusIconUri();

    /**
     * Gets the description to use for this corpus in system search settings.
     */
    CharSequence getSettingsDescription();

    /**
     * Gets the search hint text for this corpus.
     */
    CharSequence getHint();

    /**
     * @return The minimum query length for which this corpus should be queried.
     */
    int getQueryThreshold();

    boolean queryAfterZeroResults();

    boolean voiceSearchEnabled();

    Intent createSearchIntent(String query, Bundle appData);

    Intent createVoiceSearchIntent(Bundle appData);

    SuggestionData createSearchShortcut(String query);

    boolean isWebCorpus();

    /**
     * Gets the sources that this corpus uses.
     */
    Collection<Source> getSources();

    /**
     * Checks if this corpus is enabled.
     */
    boolean isCorpusEnabled();

    /**
     * Checks if this corpus is enabled by default.
     */
    boolean isCorpusDefaultEnabled();

    /**
     * Checks if this corpus should be hidden from the corpus selector.
     */
    boolean isCorpusHidden();

    /**
     * Checks if this corpus is location aware.
     */
    boolean isLocationAware();
}
