package com.droibit.accountmushroom;

import android.Manifest;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import com.droibit.accountmushroom.model.AccountFetcher;
import com.droibit.accountmushroom.model.GroupedList;
import com.droibit.accountmushroom.utils.PermissionChecker;

import java.util.Map;

/**
 * Simeji系のマッシュルームから表示されるアクティビティ。<br>
 * ログインしている全アカウントを表示し、その名前を選択できるようにする。
 *
 * @author kumagai
 */
public class MushroomActivity extends ExpandableListActivity {

    private static final int REQUEST_PERMISSION = 1;

    private static final int REQUEST_FILTER = 2;

    /** マッシュルームとやりとりする文字列のキー */
    private static final String KEY_REPLACE = "replace_key";
    /** マッシュルームのアクション */
    private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";

    private AccountFetcher mAccountFetcher;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(R.string.title_select_account);

        final String action = getIntent().getAction();
        if (TextUtils.isEmpty(action) || !ACTION_INTERCEPT.equals(action)) {
            // IMEから呼び出されていない場合は終了する
            finish();
        }
        setContentView(R.layout.activity_mushroom);

        mAccountFetcher = new AccountFetcher(this);

        // 既に権限が得られている場合は表示する
        if (PermissionChecker.hasSelfPermission(this, Manifest.permission.GET_ACCOUNTS)) {
            showAccounts();
        } else {
            PermissionChecker.requestAllPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSION);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // フィルターが書けられた OR 変更したら再表示する。
        if (requestCode == REQUEST_FILTER && resultCode == RESULT_OK) {
            showAccounts();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_PERMISSION) {
            return;
        }

        if (PermissionChecker.hasGranted(grantResults)) {
            showAccounts();
        } else {
            Toast.makeText(this, R.string.msg_deneid_permission_account,
                        Toast.LENGTH_SHORT).show();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_FILTER);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        final Map<String, String> child = (Map<String, String>) getExpandableListAdapter()
                                                                    .getChild(groupPosition, childPosition);
        final Intent data = new Intent();
        data.putExtra(KEY_REPLACE, child.get(GroupedList.KEY_CHILD_TITLE));
        setResult(RESULT_OK, data);
        finish();
        return true;
    }

    private void showAccounts() {
        // アカウト情報のリストを表示する
        final GroupedList gropedList = mAccountFetcher.fetch();
        final SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                this,
                gropedList.groups,
                android.R.layout.simple_expandable_list_item_1,
                new String []{GroupedList.KEY_GROUP_TITLE},
                new int []{android.R.id.text1},
                gropedList.childrenList,
                R.layout.simple_expandable_list_item,
                new String []{GroupedList.KEY_CHILD_TITLE},
                new int []{android.R.id.text1}
        );
        setListAdapter(adapter);
    }
}
