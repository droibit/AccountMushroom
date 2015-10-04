package com.droibit.accountmushroom.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.droibit.accountmushroom.SettingsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 端末でログインしているアカウント情報を読み込むためのユーティリティクラス。<br>
 * {@link #getLabelForType(String)}メソッドはオープンソースの設定アプリ（ChooseAccountActivity.java）より参照。
 *
 * @author kumagai
 * @since 2014/03/23.
 */
public final class AccountFetcher {

    private static final String TAG = AccountFetcher.class.getSimpleName();

    /** コンテキスト */
    private final Context mContext;

    /** {@link Account#type}とパッケージ名の対応表 */
    private final Map<String, String> mServices;

    /** 非表示にするアカウント */
    private Set<String> mIgnoreAccounts;

    /** ログイン情報群 */
    private final Map<String, AuthenticatorDescription> mTypeToAuthDescription;

    /** アカウントグループ名を比較する */
    private final Comparator<Map<String, List<String>>> mComparator = new Comparator<Map<String, List<String>>>() {
        @Override
        public int compare(Map<String, List<String>> lhs, Map<String, List<String>> rhs) {
            return lhs.keySet().toString().compareTo(rhs.keySet().toString());
        }
    };

    /**
     * 新しいインスタンスを作成する
     *
     * @param context コンテキスト
     */
    public AccountFetcher(Context context) {
        mContext = context;

        mTypeToAuthDescription = new HashMap<>();
        final AuthenticatorDescription[] authDescs = AccountManager.get(context).getAuthenticatorTypes();
        for (int i = 0; i < authDescs.length; i++) {
            mTypeToAuthDescription.put(authDescs[i].type, authDescs[i]);
        }
        mServices = new HashMap<>(mTypeToAuthDescription.size());
    }

    /**
     * 端末に追加されているアカウントを読み込む。<br>
     * {@link android.accounts.Account#type}ごとにグループ化したリストを返す。
     *
     * @return グループ化したアカウントのリスト
     */
    public final GroupedList fetch() {
        mIgnoreAccounts = SettingsActivity.getHideAccounts(mContext);

        final List<Map<String, List<String>>> groups = new ArrayList<>();
        // 扱いやすいように[親:1 - 子:多]のリストに変換する
        for (Account account : AccountManager.get(mContext).getAccounts()) {
            // 非表示アカウントの場合
            if (ignore(account.type)) {
                continue;
            }

            final String label = getLabelForType(account.type);
            // サービス名が取得できない場合
            if (TextUtils.isEmpty(label)) {
                continue;
            }

            final Map<String, List<String>> searchedGroup = getGroup(groups, label);
            if (searchedGroup == null) {
                final Map<String, List<String>> newGroup = new HashMap<String, List<String>>(1);
                final List<String> newChildren = new ArrayList<String>();
                newChildren.add(account.name);
                newGroup.put(label, newChildren);
                groups.add(newGroup);
                continue;
            }
            final List<String> existChildren = searchedGroup.get(label);
            existChildren.add(account.name);
        }

        // アカウントが存在しない場合
        if (groups.isEmpty()) {
            return new GroupedList();
        }
        Collections.sort(groups, mComparator);
        return new GroupedList(groups);
    }

    /**
     * アカウントのグループのみ読み込む。<br>
     * {@link Account#type}でグルーピングしたリストを返す
     *
     * @return アカウントのグループリスト
     */
    public Map<String, String> fetchGroups() {
        final Map<String, String> groups = new HashMap<String, String>(mTypeToAuthDescription.size());
        for (Account account : AccountManager.get(mContext).getAccounts()) {
            final String label = getLabelForType(account.type);
            if (TextUtils.isEmpty(label)) {
                continue;
            }
            if (!groups.containsKey(account.type)) {
                groups.put(label, account.type);
            }
        }
        return groups;
    }

    private boolean ignore(String accountType) {
        return mIgnoreAccounts.contains(accountType);
    }

    private Map<String, List<String>> getGroup(List<Map<String, List<String>>> groups, String accountType) {
        for (Map<String, List<String>> group : groups) {
            if (group.containsKey(accountType)) {
                return group;
            }
        }
        return null;
    }

    private String getLabelForType(String accountType) {
        // キャッシュに対応するラベルが存在する場合
        if (mServices.containsKey(accountType)) {
            return mServices.get(accountType);
        }

        String label = null;
        if (mTypeToAuthDescription.containsKey(accountType)) {
            try {
                final AuthenticatorDescription desc = mTypeToAuthDescription.get(accountType);
                final Context authContext = mContext.createPackageContext(desc.packageName, 0);
                label = authContext.getResources().getString(desc.labelId);
                // 対応表にキャッシュしておく
                if (!mServices.containsKey(accountType)) {
                    mServices.put(accountType, label);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "No label name for account type " + accountType);
            } catch (Resources.NotFoundException e) {
                Log.w(TAG, "No label resource for account type " + accountType);
            }
        }
        return label;
    }

}
