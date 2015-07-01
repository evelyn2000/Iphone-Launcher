
package com.android.ui;

import com.android.iphonelauncher.R;
import com.android.quicksearchbox.Config;
import com.android.quicksearchbox.Corpora;
import com.android.quicksearchbox.Corpus;
import com.android.quicksearchbox.LatencyTracker;
import com.android.quicksearchbox.ListSuggestionCursor;
import com.android.quicksearchbox.Logger;
import com.android.quicksearchbox.QsbApplication;
import com.android.quicksearchbox.ShortcutRepository;
import com.android.quicksearchbox.SuggestionCursor;
import com.android.quicksearchbox.SuggestionData;
import com.android.quicksearchbox.SuggestionUtils;
import com.android.quicksearchbox.Suggestions;
import com.android.quicksearchbox.SuggestionsProvider;
import com.android.quicksearchbox.VoiceSearch;
import com.android.quicksearchbox.ui.QueryTextView;
import com.android.quicksearchbox.ui.SuggestionClickListener;
import com.android.quicksearchbox.ui.SuggestionsAdapter;
import com.android.quicksearchbox.ui.SuggestionsView;
import com.google.common.base.CharMatcher;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class SearchScreen extends FrameLayout {

    private boolean DBG = false;

    private static final String TAG = "SearchScreen";

    protected QueryTextView mQueryTextView;

    // True if the query was empty on the previous call to updateQuery()
    protected boolean mQueryWasEmpty = true;

    protected SuggestionsAdapter mSuggestionsAdapter;

    // Measures time from for last onCreate()/onNewIntent() call.
    private LatencyTracker mStartLatencyTracker;

    // Whether QSB is starting. True between the calls to
    // onCreate()/onNewIntent() and onResume().
    private boolean mStarting;

    // True if the user has taken some action, e.g. launching a search, voice
    // search,
    // or suggestions, since QSB was last started.
    private boolean mTookAction;

    // private CorporaObserver mCorporaObserver;

    protected SuggestionsView mSuggestionsView;

    private Corpus mCorpus;

    private Bundle mAppSearchData;

    private boolean mUpdateSuggestions;

    private final Handler mHandler = new Handler();

    private final Runnable mUpdateSuggestionsTask = new Runnable() {
        public void run() {
            updateSuggestions(getQuery());
        }
    };

    private final Runnable mShowInputMethodTask = new Runnable() {
        public void run() {
            showInputMethodForQuery();
        }
    };

    public SearchScreen(Context context) {
        this(context, null);
    }

    public SearchScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAlwaysDrawnWithCacheEnabled(false);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();

        SuggestListFocusListener suggestionFocusListener = new SuggestListFocusListener();
        mSuggestionsAdapter = getQsbApplication().createSuggestionsAdapter();
        mSuggestionsAdapter.setSuggestionClickListener(new ClickHandler());
        mSuggestionsAdapter.setOnFocusChangeListener(suggestionFocusListener);

        mQueryTextView = (QueryTextView) findViewById(R.id.search_src_text);
        mSuggestionsView = (SuggestionsView) findViewById(R.id.suggestions);
        mSuggestionsView.setOnScrollListener(new InputMethodCloser());
        mSuggestionsView.setOnKeyListener(new SuggestionsViewKeyListener());
        mSuggestionsView.setOnFocusChangeListener(suggestionFocusListener);

        mQueryTextView.addTextChangedListener(new SearchTextWatcher());
        mQueryTextView.setOnKeyListener(new QueryTextViewKeyListener());
        mQueryTextView.setOnFocusChangeListener(new QueryTextViewFocusListener());
        mQueryTextView.setSuggestionClickListener(new ClickHandler());

        mUpdateSuggestions = true;

        mSuggestionsAdapter.registerDataSetObserver(new SuggestionsObserver());

        // Do this at the end, to avoid updating the list view when setSource()
        // is called.
        mSuggestionsView.setAdapter(mSuggestionsAdapter);

        // mCorporaObserver = new CorporaObserver();
        // getCorpora().registerDataSetObserver(mCorporaObserver);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Launch the IME after a bit
            mHandler.postDelayed(mShowInputMethodTask, 0);
        }
    }

    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void setChildrenDrawingCacheEnabled(boolean enabled) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            view.buildDrawingCache(true);
        }
    }

    public void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        super.setChildrenDrawnWithCacheEnabled(enabled);
    }

    /**
     * for search fun
     */

    /**
     * Hides the input method.
     */
    protected void hideInputMethod() {
        mQueryTextView.hideInputMethod();
    }

    protected void showInputMethodForQuery() {
        mQueryTextView.showInputMethod();
    }

    protected void onSuggestionListFocusChange(boolean focused) {
    }

    protected void onQueryTextViewFocusChange(boolean focused) {
    }

    /**
     * Hides the input method when the suggestions get focus.
     */
    private class SuggestListFocusListener implements OnFocusChangeListener {
        public void onFocusChange(View v, boolean focused) {
            if (DBG)
                Log.d(TAG, "Suggestions focus change, now: " + focused);
            if (focused) {
                // The suggestions list got focus, hide the input method
                hideInputMethod();
            }
            onSuggestionListFocusChange(focused);
        }
    }

    private Corpus getCorpus(String sourceName) {
        if (sourceName == null)
            return null;
        Corpus corpus = getCorpora().getCorpus(sourceName);
        if (corpus == null) {
            Log.w(TAG, "Unknown corpus " + sourceName);
            return null;
        }
        return corpus;
    }

    private void setCorpus(String corpusName) {
        if (DBG)
            Log.d(TAG, "setCorpus(" + corpusName + ")");
        mCorpus = getCorpus(corpusName);
        // Drawable sourceIcon;
        // if (mCorpus == null) {
        // sourceIcon = getCorpusViewFactory().getGlobalSearchIcon();
        // } else {
        // sourceIcon = mCorpus.getCorpusIcon();
        // }
        mSuggestionsAdapter.setCorpus(mCorpus);
    }

    private String getCorpusName() {
        return mCorpus == null ? null : mCorpus.getName();
    }

    private QsbApplication getQsbApplication() {
        return QsbApplication.get(getContext());
    }

    private Config getConfig() {
        return getQsbApplication().getConfig();
    }

    private Corpora getCorpora() {
        return getQsbApplication().getCorpora();
    }

    private ShortcutRepository getShortcutRepository() {
        return getQsbApplication().getShortcutRepository();
    }

    private SuggestionsProvider getSuggestionsProvider() {
        return getQsbApplication().getSuggestionsProvider();
    }

    // private CorpusViewFactory getCorpusViewFactory() {
    // return getQsbApplication().getCorpusViewFactory();
    // }

    private VoiceSearch getVoiceSearch() {
        return QsbApplication.get(getContext()).getVoiceSearch();
    }

    private Logger getLogger() {
        return getQsbApplication().getLogger();
    }

    protected SuggestionCursor getCurrentSuggestions() {
        return mSuggestionsAdapter.getCurrentSuggestions();
    }

    protected SuggestionCursor getCurrentSuggestions(int position) {
        SuggestionCursor suggestions = getCurrentSuggestions();
        if (suggestions == null) {
            return null;
        }
        int count = suggestions.getCount();
        if (position < 0 || position >= count) {
            Log.w(TAG, "Invalid suggestion position " + position + ", count = " + count);
            return null;
        }
        suggestions.moveTo(position);
        return suggestions;
    }

    protected Set<Corpus> getCurrentIncludedCorpora() {
        Suggestions suggestions = mSuggestionsAdapter.getSuggestions();
        return suggestions == null ? null : suggestions.getIncludedCorpora();
    }

    protected void launchIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        try {
            getContext().startActivity(intent);
        } catch (RuntimeException ex) {
            // Since the intents for suggestions specified by suggestion
            // providers,
            // guard against them not being handled, not allowed, etc.
            Log.e(TAG, "Failed to start " + intent.toUri(0), ex);
        }
    }

    protected boolean launchSuggestion(int position) {
        SuggestionCursor suggestions = getCurrentSuggestions(position);
        if (suggestions == null)
            return false;

        if (DBG)
            Log.d(TAG, "Launching suggestion " + position);
        mTookAction = true;

        // Log suggestion click
        getLogger().logSuggestionClick(position, suggestions, getCurrentIncludedCorpora(),
                Logger.SUGGESTION_CLICK_TYPE_LAUNCH);

        // Create shortcut
        getShortcutRepository().reportClick(suggestions, position);

        // Launch intent
        suggestions.moveTo(position);
        Intent intent = SuggestionUtils.getSuggestionIntent(suggestions, mAppSearchData);
        launchIntent(intent);

        return true;
    }

    protected void clickedQuickContact(int position) {
        SuggestionCursor suggestions = getCurrentSuggestions(position);
        if (suggestions == null)
            return;

        if (DBG)
            Log.d(TAG, "Used suggestion " + position);
        mTookAction = true;

        // Log suggestion click
        getLogger().logSuggestionClick(position, suggestions, getCurrentIncludedCorpora(),
                Logger.SUGGESTION_CLICK_TYPE_QUICK_CONTACT);

        // Create shortcut
        getShortcutRepository().reportClick(suggestions, position);
    }

    protected boolean onSuggestionLongClicked(int position) {
        if (DBG)
            Log.d(TAG, "Long clicked on suggestion " + position);
        return false;
    }

    /**
     * Sets the text in the query box. Does not update the suggestions.
     */
    private void setQuery(String query, boolean selectAll) {
        mUpdateSuggestions = false;
        mQueryTextView.setText(query);
        mQueryTextView.setTextSelection(selectAll);
        mUpdateSuggestions = true;
    }

    protected void updateSuggestions(String query) {

        query = CharMatcher.WHITESPACE.trimLeadingFrom(query);
        if (DBG)
            Log.d(TAG, "getSuggestions(\"" + query + "\"," + mCorpus + "," + getMaxSuggestions()
                    + ")");
        Suggestions suggestions = getSuggestionsProvider().getSuggestions(query, mCorpus,
                getMaxSuggestions());

        // Log start latency if this is the first suggestions update
        if (mStarting) {
            mStarting = false;
            // TODO:
            // String source = getIntent().getStringExtra(Search.SOURCE);
            // int latency = mStartLatencyTracker.getLatency();
            // getLogger().logStart(latency, source, mCorpus,
            // suggestions.getExpectedCorpora());
            // getQsbApplication().onStartupComplete();
        }

        mSuggestionsAdapter.setSuggestions(suggestions);
    }

    private int getMaxSuggestions() {
        Config config = getConfig();
        return mCorpus == null ? config.getMaxPromotedSuggestions() : config
                .getMaxResultsPerSource();
    }

    protected void refineSuggestion(int position) {
        if (DBG)
            Log.d(TAG, "query refine clicked, pos " + position);
        SuggestionCursor suggestions = getCurrentSuggestions(position);
        if (suggestions == null) {
            return;
        }
        String query = suggestions.getSuggestionQuery();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        // Log refine click
        getLogger().logSuggestionClick(position, suggestions, getCurrentIncludedCorpora(),
                Logger.SUGGESTION_CLICK_TYPE_REFINE);

        // Put query + space in query text view
        String queryWithSpace = query + ' ';
        setQuery(queryWithSpace, false);
        updateSuggestions(queryWithSpace);
        mQueryTextView.requestFocus();
    }

    private class ClickHandler implements SuggestionClickListener {
        public void onSuggestionClicked(int position) {
            launchSuggestion(position);
        }

        public void onSuggestionQuickContactClicked(int position) {
            clickedQuickContact(position);
        }

        public boolean onSuggestionLongClicked(int position) {
            return SearchScreen.this.onSuggestionLongClicked(position);
        }

        public void onSuggestionQueryRefineClicked(int position) {
            refineSuggestion(position);
        }
    }

    private class InputMethodCloser implements SuggestionsView.OnScrollListener {

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            hideInputMethod();
        }
    }

    /**
     * Handles key events on the suggestions list view.
     */
    private class SuggestionsViewKeyListener implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                int position = getSelectedPosition();
                if (onSuggestionKeyDown(position, keyCode, event)) {
                    return true;
                }
            }
            return forwardKeyToQueryTextView(keyCode, event);
        }
    }

    protected boolean onSuggestionKeyDown(int position, int keyCode, KeyEvent event) {
        // Treat enter or search as a click
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            return launchSuggestion(position);
        }

        return false;
    }

    protected int getSelectedPosition() {
        return mSuggestionsView.getSelectedPosition();
    }

    private boolean forwardKeyToQueryTextView(int keyCode, KeyEvent event) {
        if (!event.isSystem() && !isDpadKey(keyCode)) {
            if (DBG)
                Log.d(TAG, "Forwarding key to query box: " + event);
            if (mQueryTextView.requestFocus()) {
                return mQueryTextView.dispatchKeyEvent(event);
            }
        }
        return false;
    }

    private boolean isDpadKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                return true;
            default:
                return false;
        }
    }

    /**
     * Filters the suggestions list when the search text changes.
     */
    private class SearchTextWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            boolean empty = s.length() == 0;
            if (empty != mQueryWasEmpty) {
                mQueryWasEmpty = empty;
                // updateUi(empty);
            }
            if (mUpdateSuggestions) {
                if (empty) {
                    mSuggestionsAdapter.setSuggestions(null);
                } else {
                    updateSuggestionsBuffered();
                }
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private void updateSuggestionsBuffered() {
        mHandler.removeCallbacks(mUpdateSuggestionsTask);
        long delay = getConfig().getTypingUpdateSuggestionsDelayMillis();
        mHandler.postDelayed(mUpdateSuggestionsTask, delay);
    }

    /**
     * Handles non-text keys in the query text view.
     */
    private class QueryTextViewKeyListener implements View.OnKeyListener {
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            // Handle IME search action key
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                // if no action was taken, consume the key event so that the
                // keyboard
                // remains on screen.
                return !onSearchClicked(Logger.SEARCH_METHOD_KEYBOARD);
            }
            return false;
        }
    }

    /**
     * @return true if a search was performed as a result of this click, false
     *         otherwise.
     */
    protected boolean onSearchClicked(int method) {
        String query = CharMatcher.WHITESPACE.trimAndCollapseFrom(getQuery(), ' ');
        if (DBG)
            Log.d(TAG, "Search clicked, query=" + query);

        // Don't do empty queries
        if (TextUtils.getTrimmedLength(query) == 0)
            return false;

        Corpus searchCorpus = getSearchCorpus();
        if (searchCorpus == null)
            return false;

        mTookAction = true;

        // Log search start
        getLogger().logSearch(mCorpus, method, query.length());

        // Create shortcut
        SuggestionData searchShortcut = searchCorpus.createSearchShortcut(query);
        if (searchShortcut != null) {
            ListSuggestionCursor cursor = new ListSuggestionCursor(query);
            cursor.add(searchShortcut);
            getShortcutRepository().reportClick(cursor, 0);
        }

        // Start search
        Intent intent = searchCorpus.createSearchIntent(query, mAppSearchData);
        launchIntent(intent);
        return true;
    }

    protected String getQuery() {
        CharSequence q = mQueryTextView.getText();
        return q == null ? "" : q.toString();
    }

    /**
     * Gets the corpus to use for any searches. This is the web corpus in "All"
     * mode, and the selected corpus otherwise.
     */
    protected Corpus getSearchCorpus() {
        if (mCorpus != null) {
            return mCorpus;
        } else {
            Corpus webCorpus = getCorpora().getWebCorpus();
            if (webCorpus == null) {
                Log.e(TAG, "No web corpus");
            }
            return webCorpus;
        }
    }

    private class QueryTextViewFocusListener implements OnFocusChangeListener {
        public void onFocusChange(View v, boolean focused) {
            if (DBG)
                Log.d(TAG, "Query focus change, now: " + focused);
            if (focused) {
                // The query box got focus, show the input method
                showInputMethodForQuery();
            } else {
                hideInputMethod();
            }
            onQueryTextViewFocusChange(focused);
        }
    }

    private class CorporaObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            setCorpus(getCorpusName());
            updateSuggestions(getQuery());
        }
    }

    private class SuggestionsObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            updateInputMethodSuggestions();
        }
    }

    /**
     * If the input method is in fullscreen mode, and the selector corpus is All
     * or Web, use the web search suggestions as completions.
     */
    protected void updateInputMethodSuggestions() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm == null || !imm.isFullscreenMode())
            return;
        Suggestions suggestions = mSuggestionsAdapter.getSuggestions();
        if (suggestions == null)
            return;
        SuggestionCursor cursor = suggestions.getPromoted();
        if (cursor == null)
            return;
        CompletionInfo[] completions = webSuggestionsToCompletions(cursor);
        if (DBG)
            Log.d(TAG, "displayCompletions(" + Arrays.toString(completions) + ")");
        imm.displayCompletions(mQueryTextView, completions);
    }

    private CompletionInfo[] webSuggestionsToCompletions(SuggestionCursor cursor) {
        int count = cursor.getCount();
        ArrayList<CompletionInfo> completions = new ArrayList<CompletionInfo>(count);
        boolean usingWebCorpus = isSearchCorpusWeb();
        for (int i = 0; i < count; i++) {
            cursor.moveTo(i);
            if (!usingWebCorpus || cursor.isWebSearchSuggestion()) {
                String text1 = cursor.getSuggestionText1();
                completions.add(new CompletionInfo(i, i, text1));
            }
        }
        return completions.toArray(new CompletionInfo[completions.size()]);
    }

    /**
     * Checks if the corpus used for typed searchs is the web corpus.
     */
    protected boolean isSearchCorpusWeb() {
        Corpus corpus = getSearchCorpus();
        return corpus != null && corpus.isWebCorpus();
    }

    public void requestQueryEditTextFocus() {
        if (mQueryTextView != null) {
            mQueryTextView.requestFocus();
        }
    }
}
