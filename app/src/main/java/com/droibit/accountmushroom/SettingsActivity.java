package com.droibit.accountmushroom;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebViewFragment;
import android.widget.Toast;

import com.droibit.accountmushroom.model.AccountFetcher;
import com.droibit.accountmushroom.utils.PermissionChecker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * マッシュルームのアカウントリストに表示しない項目を設定するためのアクティビティ
 *
 * @author kumagai
 */
public class SettingsActivity extends Activity {

    private static final int REQUEST_PERMISSION = 1;

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

    /** {@inheritDoc} */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_PERMISSION) {
            return;
        }

        final Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
        if (fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * アプリケーションの設定を行うためのフラグメント
     */
    public static class SettingsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        /** 複数選択のプレファレンス */
        private MultiSelectListPreference mListPref;

        /** {@inheritDoc} */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref);

            mListPref = (MultiSelectListPreference)findPreference(getString(R.string.pref_filter_key_select_account));
            mListPref.setOnPreferenceChangeListener(this);
        }

        /** {@inheritDoc} */
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            if (!PermissionChecker.hasSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS)) {
                PermissionChecker.requestAllPermissions(getActivity(), new String[]{Manifest.permission.GET_ACCOUNTS},
                        REQUEST_PERMISSION);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            if (PermissionChecker.hasSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS)) {
                setupAccounts(mListPref);
            } else {
                mListPref.setEnabled(false);
            }
        }

        /** {@inheritDoc} */
        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (PermissionChecker.hasGranted(grantResults)) {
                setupAccounts(mListPref);
            } else {
                Toast.makeText(getActivity(), R.string.msg_deneid_permission_accout_in_settings,
                        Toast.LENGTH_SHORT).show();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void onResume() {
            super.onResume();

            // 設定アプリから許可されて場合にそなえて、アカウントリストがなければ読みこむようにする
            if (PermissionChecker.hasSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS)) {
                if (mListPref.getEntries() == null || mListPref.getEntries().length == 0) {
                    setupAccounts(mListPref);
                }
            } else {
                mListPref.setEnabled(false);
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            getActivity().setResult(Activity.RESULT_OK);
            return true;
        }

        private void setupAccounts(MultiSelectListPreference listPref) {
            final AccountFetcher fetcher = new AccountFetcher(getActivity());
            final Map<String, String> groups = fetcher.fetchGroups();

            if (!groups.isEmpty()) {
                listPref.setEntries(groups.keySet().toArray(new String[groups.size()]));
                listPref.setEntryValues(groups.values().toArray(new String[groups.size()]));
                listPref.setEnabled(true);
            } else {
                listPref.setEnabled(false);
            }
        }
    }

    public static Set<String> getHideAccounts(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(context.getString(R.string.pref_filter_key_select_account), new HashSet<String>());
    }
}
