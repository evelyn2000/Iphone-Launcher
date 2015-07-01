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

/**
 * Interface for individual suggestions.
 */
public interface Suggestion {

    /**
     * Gets the source that produced the current suggestion.
     */
    Source getSuggestionSource();

    /**
     * Gets the shortcut ID of the current suggestion.
     */
    String getShortcutId();

    /**
     * Whether to show a spinner while refreshing this shortcut.
     */
    boolean isSpinnerWhileRefreshing();

    /**
     * Gets the format of the text returned by {@link #getSuggestionText1()}
     * and {@link #getSuggestionText2()}.
     *
     * @return {@code null} or "html"
     */
    String getSuggestionFormat();

    /**
     * Gets the first text line for the current suggestion.
     */
    String getSuggestionText1();

    /**
     * Gets the second text line for the current suggestion.
     */
    String getSuggestionText2();

    /**
     * Gets the second text line URL for the current suggestion.
     */
    String getSuggestionText2Url();

    /**
     * Gets the left-hand-side icon for the current suggestion.
     *
     * @return A string that can be passed to {@link Source#getIcon(String)}.
     */
    String getSuggestionIcon1();

    /**
     * Gets the right-hand-side icon for the current suggestion.
     *
     * @return A string that can be passed to {@link Source#getIcon(String)}.
     */
    String getSuggestionIcon2();

    /**
     * Gets the intent action for the current suggestion.
     */
    String getSuggestionIntentAction();

    /**
     * Gets the extra data associated with this suggestion's intent.
     */
    String getSuggestionIntentExtraData();

    /**
     * Gets the data associated with this suggestion's intent.
     */
    String getSuggestionIntentDataString();

    /**
     * Gets the query associated with this suggestion's intent.
     */
    String getSuggestionQuery();

    /**
     * Gets the suggestion log type for the current suggestion. This is logged together
     * with the value returned from {@link Source#getName()}.
     * The value is source-specific. Most sources return {@code null}.
     */
    String getSuggestionLogType();

    /**
     * Checks if this suggestion is a shortcut.
     */
    boolean isSuggestionShortcut();

    /**
     * Checks if this is a web search suggestion.
     */
    boolean isWebSearchSuggestion();

}
