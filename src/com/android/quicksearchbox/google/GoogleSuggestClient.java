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
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
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

package com.android.quicksearchbox.google;

import com.android.iphonelauncher.R;
import com.android.quicksearchbox.QsbApplication;
import com.android.quicksearchbox.Source;
import com.android.quicksearchbox.SourceResult;
import com.android.quicksearchbox.SuggestionCursor;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Use network-based Google Suggests to provide search suggestions.
 */
public class GoogleSuggestClient extends GoogleSource {

    private static final boolean DBG = false;
    private static final String LOG_TAG = "GoogleSearch";

    private static final String USER_AGENT = "Android/" + Build.VERSION.RELEASE;
    private String mSuggestUri;
    private static final int HTTP_TIMEOUT_MS = 1000;

    // TODO: this should be defined somewhere
    private static final String HTTP_TIMEOUT = "http.connection-manager.timeout";

    private final HttpClient mHttpClient;
    
    private Context mContext;

    public GoogleSuggestClient(Context context) {
        super(context);
        mContext = context;
        mHttpClient = new DefaultHttpClient();
        HttpParams params = mHttpClient.getParams();
        HttpProtocolParams.setUserAgent(params, USER_AGENT);
        params.setLongParameter(HTTP_TIMEOUT, HTTP_TIMEOUT_MS);

        // NOTE:  Do not look up the resource here;  Localization changes may not have completed
        // yet (e.g. we may still be reading the SIM card).
        mSuggestUri = null;
    }

    @Override
    public ComponentName getIntentComponent() {
        return new ComponentName(getContext(), GoogleSearch.class);
    }

    @Override
    public boolean isLocationAware() {
        return false;
    }

    @Override
    public SourceResult queryInternal(String query) {
        return getSuggestions(query);
    }

    @Override
    public SourceResult queryExternal(String query) {
        return query(query);
    }
    
    /**
     * Queries for a given search term and returns a cursor containing
     * suggestions ordered by best match.
     */
    public SourceResult getSuggestions(String query) {
        if (TextUtils.isEmpty(query)) {
            return null;
        }
        if (!isNetworkConnected()) {
            Log.i(LOG_TAG, "Not connected to network.");
            return null;
        }
    	String suggestUri = QsbApplication.get(mContext).getSearchEngineInfo().getSuggestUriForQuery(query);
        if (TextUtils.isEmpty(suggestUri)) {
            // No suggest URI available for this engine
            return null;
        }

        try {
            String content = readUrl(suggestUri);
            if (content == null) return null;
            JSONArray suggestions;
			JSONArray descriptions;
            
            /*
             * Used for Baidu search suggestions only, because Baidu suggestions data format is as follows:
             * window.baidu.sug({q:"xx",p:false,s:["xx","xx","xx","xx","xx","xx","xx","xx","xx","xx"]});
             * so just cut extra letters and then construct json arrays.
             */
            if (QsbApplication.get(mContext).getSearchEngineInfo().getName().equals("baidu")) {
            	content = content.substring(17, content.length() - 2);
            	
            	JSONObject obj = new JSONObject(content);
				suggestions = obj.getJSONArray("s");
				descriptions = null;
            } else {
            	/* The data format is a JSON array with items being regular strings or JSON arrays
                 * themselves. We are interested in the second and third elements, both of which
                 * should be JSON arrays. The second element/array contains the suggestions and the
                 * third element contains the descriptions. Some search engines don't support
                 * suggestion descriptions so the third element is optional.
                 */
                JSONArray results = new JSONArray(content);
                suggestions = results.getJSONArray(1);
                descriptions = null;
                if (results.length() > 2) {
                    descriptions = results.getJSONArray(2);
                    // Some search engines given an empty array "[]" for descriptions instead of
                    // not including it in the response.
                    if (descriptions.length() == 0) {
                        descriptions = null;
                    }
                }
            }
            return new GoogleSuggestCursor(this, query, suggestions, descriptions);
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Error", e);
        }
        return null;
    }
    
    /**
     * Executes a GET request and returns the response content.
     *
     * @param url Request URI.
     * @param requestHeaders Request headers.
     * @return The response content. This is the empty string if the response
     *         contained no content.
     */
    public String readUrl(String url) {
        try {
            HttpGet method = new HttpGet(url);
            HttpResponse response = mHttpClient.execute(method);
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity());
            } else {
                Log.i(LOG_TAG, "Suggestion request failed");
                return null;
            }
        } catch (IOException e) {
            Log.w(LOG_TAG, "Error", e);
            return null;
        }
    }

    /**
     * Queries for a given search term and returns a cursor containing
     * suggestions ordered by best match.
     */
    private SourceResult query(String query) {
        if (TextUtils.isEmpty(query)) {
            return null;
        }
        if (!isNetworkConnected()) {
            Log.i(LOG_TAG, "Not connected to network.");
            return null;
        }
        try {
            query = URLEncoder.encode(query, "UTF-8");
            // NOTE:  This code uses resources to optionally select the search Uri, based on the
            // MCC value from the SIM.  iThe default string will most likely be fine.  It is
            // paramerterized to accept info from the Locale, the language code is the first
            // parameter (%1$s) and the country code is the second (%2$s).  This code *must*
            // function in the same way as a similar lookup in
            // com.android.browser.BrowserActivity#onCreate().  If you change
            // either of these functions, change them both.  (The same is true for the underlying
            // resource strings, which are stored in mcc-specific xml files.)
            if (mSuggestUri == null) {
                Locale l = Locale.getDefault();
                String language = l.getLanguage();
                String country = l.getCountry().toLowerCase();
                // Chinese and Portuguese have two langauge variants.
                if ("zh".equals(language)) {
                    if ("cn".equals(country)) {
                        language = "zh-CN";
                    } else if ("tw".equals(country)) {
                        language = "zh-TW";
                    }
                } else if ("pt".equals(language)) {
                    if ("br".equals(country)) {
                        language = "pt-BR";
                    } else if ("pt".equals(country)) {
                        language = "pt-PT";
                    }
                }
                mSuggestUri = getContext().getResources().getString(R.string.google_suggest_base,
                                                                    language,
                                                                    country);
            }

            String suggestUri = mSuggestUri + query;
            if (DBG) Log.d(LOG_TAG, "Sending request: " + suggestUri);
            HttpGet method = new HttpGet(suggestUri);
            HttpResponse response = mHttpClient.execute(method);
            if (response.getStatusLine().getStatusCode() == 200) {

                /* Goto http://www.google.com/complete/search?json=true&q=foo
                 * to see what the data format looks like. It's basically a json
                 * array containing 4 other arrays. We only care about the middle
                 * 2 which contain the suggestions and their popularity.
                 */
                JSONArray results = new JSONArray(EntityUtils.toString(response.getEntity()));
                JSONArray suggestions = results.getJSONArray(1);
                JSONArray popularity = results.getJSONArray(2);
                if (DBG) Log.d(LOG_TAG, "Got " + suggestions.length() + " results");
                return new GoogleSuggestCursor(this, query, suggestions, popularity);
            } else {
                if (DBG) Log.d(LOG_TAG, "Request failed " + response.getStatusLine());
            }
        } catch (UnsupportedEncodingException e) {
            Log.w(LOG_TAG, "Error", e);
        } catch (IOException e) {
            Log.w(LOG_TAG, "Error", e);
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Error", e);
        }
        return null;
    }

    @Override
    public SuggestionCursor refreshShortcut(String shortcutId, String oldExtraData) {
        return null;
    }

    private boolean isNetworkConnected() {
        NetworkInfo networkInfo = getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivity =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return null;
        }
        return connectivity.getActiveNetworkInfo();
    }

    private static class GoogleSuggestCursor extends AbstractGoogleSourceResult {

        /* Contains the actual suggestions */
        private final JSONArray mSuggestions;

        /* This contains the popularity of each suggestion
         * i.e. 165,000 results. It's not related to sorting.
         */
        private final JSONArray mPopularity;

        public GoogleSuggestCursor(Source source, String userQuery,
                JSONArray suggestions, JSONArray popularity) {
            super(source, userQuery);
            mSuggestions = suggestions;
            mPopularity = popularity;
        }

        @Override
        public int getCount() {
            return mSuggestions.length();
        }

        @Override
        public String getSuggestionQuery() {
            try {
                return mSuggestions.getString(getPosition());
            } catch (JSONException e) {
                Log.w(LOG_TAG, "Error parsing response: " + e);
                return null;
            }
        }

        @Override
        public String getSuggestionText2() {
        	if (mPopularity == null) {
        		return null;
        	}
            try {
                return mPopularity.getString(getPosition());
            } catch (JSONException e) {
                Log.w(LOG_TAG, "Error parsing response: " + e);
                return null;
            }
        }
    }
}
