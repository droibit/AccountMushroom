package com.droibit.accountmushroom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * マッシュルームのアカウントリストに表示しない項目を設定するためのアクティビティ
 *
 * @author kumagai
 * @since 2014/03/23.
 */
public class SettingsActivity extends Activity {

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getActionBar().setTitle(R.string.title_activity_settings);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    /**
     * アプリケーションの設定を行うためのフラグメント
     */
    public static class SettingsFragment extends PreferenceFragment {

        /** 複数選択のプレファレンス */
        private MultiSelectListPreference mListPref;

        /** {@inheritDoc} */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref);

            mListPref = (MultiSelectListPreference)findPreference(
                    getString(R.string.pref_filter_key_select_account));

            final AccountFetcher fetcher = new AccountFetcher(getActivity());
            final Map<String, String> groups = fetcher.fetchGroups();

            if (!groups.isEmpty()) {
                mListPref.setEntries(groups.keySet().toArray(new String[groups.size()]));
                mListPref.setEntryValues(groups.values().toArray(new String[groups.size()]));
            } else {
                mListPref.setEnabled(false);
            }
        }
    }

    public static Set<String> getHideAccounts(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(context.getString(R.string.pref_filter_key_select_account), new HashSet<String>());
    }
}
