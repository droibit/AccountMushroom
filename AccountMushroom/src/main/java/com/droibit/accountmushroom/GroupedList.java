package com.droibit.accountmushroom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link android.widget.ExpandableListView}で表示する親グループと子アイテムを格納するクラス
 *
 * @author kumagai
 * @since 2014/03/23.
 */
public class GroupedList {

    /** 親グループの{@link java.util.Map}のキー */
    public static final String KEY_GROUP_TITLE = "Group";

    /** 子アイテムの{@link java.util.Map}のキー */
    public static final String KEY_CHILD_TITLE = "Child";

    /** 親グループのリスト */
    public List<Map<String, String>> groups;

    /** 子アイテムのリスト */
    public List<List<Map<String, String>>> childrenList;

    /**
     * 新しいインスタンスを作成する
     */
    public GroupedList() {
        this(null);
    }

    /**
     * 新しいインスタンスを作成する
     *
     * @param srcGroups グルーピングしたアカウント情報
     */
    public GroupedList(List<Map<String, List<String>>> srcGroups) {
        this.groups = new ArrayList<Map<String, String>>();
        this.childrenList = new ArrayList<List<Map<String, String>>>();

        if (srcGroups != null) {
            initialize(srcGroups);
        }
    }

    private void initialize(List<Map<String,List<String>>> srcGroups) {
        for (Map<String, List<String>> group : srcGroups) {
            createNewGroup(group);
        }
    }

    private void createNewGroup(Map<String, List<String>> srcGroup) {
        for (String key : srcGroup.keySet()) {
            // 親グループを作成する
            final Map<String, String> newGroup = new HashMap<String, String>(1);
            newGroup.put(KEY_GROUP_TITLE, key);
            groups.add(newGroup);

            // 子アイテムリストを作成する
            final List<String> items = srcGroup.get(key);
            final List<Map<String, String>> children = new ArrayList<Map<String, String>>(items.size());
            for (String item : items) {
                final Map<String, String> child = new HashMap<String, String>(1);
                child.put(KEY_CHILD_TITLE, item);
                children.add(child);
            }
            childrenList.add(children);
        }
    }

    /**
     * 新しい親グループを追加する
     *
     * @param title グループのタイトル
     */
    public void addGroup(String title) {
        if (!containsGroup(title)) {
            final Map<String, String> newGroup = new HashMap<String, String>();
            newGroup.put(KEY_GROUP_TITLE, title);
            groups.add(newGroup);
        }
    }

    /**
     * 新しい子アイテムを追加する
     *
     * @param title アイテムのタイトル
     */
    public void addChild(String type, String title) {
        final int index = getIndex(type);
        if (index == 0) {
            final List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            final Map<String, String> child = new HashMap<String, String>();
            child.put(KEY_CHILD_TITLE, title);
            children.add(child);
            childrenList.add(children);
        }
    }

    private boolean containsGroup(String type) {
        for (Map<String, String> group : groups) {
            if (group.containsValue(type)) {
                return true;
            }
        }
        return false;
    }

    private int getIndex(String type) {
        for (int i = 0, size= groups.size(); i < size; i++) {
            if (groups.get(i).containsValue(type)) {
                return i;
            }
        }
        // 必ず先にグループを作成するので、この場合は例外を投げる
        throw new IllegalArgumentException();
    }
}
