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


import com.android.iphonelauncher.R;
import com.android.quicksearchbox.util.Util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * The apps search source.
 */
public class AppsCorpus extends SingleSourceCorpus {

    private static final String TAG = "QSB.AppsCorpus";

    private static final String APPS_CORPUS_NAME = "apps";

    public AppsCorpus(Context context, Config config, Source appsSource) {
        super(context, config, appsSource);
    }

    @Override
    public CharSequence getLabel() {
        return getContext().getText(R.string.corpus_label_apps);
    }

    @Override
    public CharSequence getHint() {
        return getContext().getText(R.string.corpus_hint_apps);
    }

    @Override
    public Drawable getCorpusIcon() {
        return getContext().getResources().getDrawable(R.drawable.corpus_icon_apps);
    }

    @Override
    public Uri getCorpusIconUri() {
        return Util.getResourceUri(getContext(), R.drawable.corpus_icon_apps);
    }

    @Override
    public String getName() {
        return APPS_CORPUS_NAME;
    }

    @Override
    public CharSequence getSettingsDescription() {
        return getContext().getText(R.string.corpus_description_apps);
    }

    @Override
    public Intent createSearchIntent(String query, Bundle appData) {
        Intent appSearchIntent = createAppSearchIntent(query, appData);
        if (appSearchIntent != null) {
            return appSearchIntent;
        } else {
            // Fall back to sending the intent to ApplicationsProvider
            return super.createSearchIntent(query, appData);
        }
    }

    /**
     * Creates an intent that starts the search activity specified in
     * R.string.apps_search_activity.
     *
     * @return An intent, or {@code null} if the search activity is not set or can't be found.
     */
    private Intent createAppSearchIntent(String query, Bundle appData) {
        ComponentName name = getComponentName(getContext(), R.string.apps_search_activity);
        if (name == null) return null;
        Intent intent = AbstractSource.createSourceSearchIntent(name, query, appData);
        if (intent == null) return null;
        ActivityInfo ai = intent.resolveActivityInfo(getContext().getPackageManager(), 0);
        if (ai != null) {
            return intent;
        } else {
            Log.w(TAG, "Can't find app search activity " + name);
            return null;
        }
    }

    private static ComponentName getComponentName(Context context, int res) {
        String nameStr = context.getString(res);
        if (TextUtils.isEmpty(nameStr)) {
            return null;
        } else {
            return ComponentName.unflattenFromString(nameStr);
        }
    }
}
