package com.droibit.accountmushroom;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import java.util.Map;

/**
 * Simeji系のマッシュルームから表示されるアクティビティ。<br>
 * ログインしている全アカウントを表示し、その名前を選択できるようにする。
 *
 * @author kumagai
 * @since 2014/03/23
 */
public class MushroomActivity extends ExpandableListActivity {

    /** マッシュルームとやりとりする文字列のキー */
    private static final String KEY_REPLACE = "replace_key";

    /** マッシュルームのアクション */
    private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String action = getIntent().getAction();
        if (TextUtils.isEmpty(action) || !ACTION_INTERCEPT.equals(action)) {
            // IMEから呼び出されていない場合は終了する
            finish();
        }
        setContentView(R.layout.activity_mushroom);

        // アカウト情報のリストを表示する
        final AccountFetcher fetcher = new AccountFetcher(this);
        final GroupedList gropedList = fetcher.fetch();
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
}
