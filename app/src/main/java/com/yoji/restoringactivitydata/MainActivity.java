package com.yoji.restoringactivitydata;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;

    private final String TEXT_KEY = "text_key";
    private final String NUM_OF_SYMBOL_KEY = "num_of_symbol_key";
    private final String ITEM_QTY_KEY = "item_qty_key";
    private final String ITEM_KEY = "item_";
    private List<Map<String, String>> content;
    private SharedPreferences itemList;
    private String[] values;
    private BaseAdapter listSimpleAdapter;
    private ArrayList<Integer> deletedIndexes;
    private final String DELETED_KEY = "deleted";

    private final AdapterView.OnItemClickListener listOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            content.remove(position);
            deletedIndexes.add(position);
            listSimpleAdapter.notifyDataSetChanged();
        }
    };

    private final SwipeRefreshLayout.OnRefreshListener swipeOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            getContentFromSharedPrefs();
            content.clear();
            initList();
            listSimpleAdapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, R.string.data_updated_message, Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView list = findViewById(R.id.list);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshId);

        itemList = getSharedPreferences("Item List", MODE_PRIVATE);
        content = new ArrayList<>();
        deletedIndexes = new ArrayList<>();

        if (itemList.getInt(ITEM_QTY_KEY, 0) == 0) {
            values = prepareContent();
            putValuesToSharedPrefs(values);
        } else {
            getContentFromSharedPrefs();
        }
        initList();

        listSimpleAdapter = createSimpleAdapter();

        list.setAdapter(listSimpleAdapter);
        list.setOnItemClickListener(listOnItemClickListener);
        swipeRefreshLayout.setOnRefreshListener(swipeOnRefreshListener);
    }

    private BaseAdapter createSimpleAdapter() {
        return new SimpleAdapter(this, content, R.layout.item_to_display,
                new String[]{TEXT_KEY, NUM_OF_SYMBOL_KEY}, new int[]{R.id.mainTextTxtViewId,
                R.id.numOfSymbolTxtViewId});
    }

    @NonNull
    private String[] prepareContent() {
        return getString(R.string.large_text).split("\n\n");
    }

    private void putValuesToSharedPrefs(String[] values) {
        SharedPreferences.Editor editor = itemList.edit();
        for (int i = 0; i < values.length; i++) {
            editor.putString(ITEM_KEY + i, values[i]);
            editor.putInt(ITEM_QTY_KEY, i);
        }
        editor.apply();
    }

    private void getContentFromSharedPrefs (){
        values = new String[itemList.getInt(ITEM_QTY_KEY, 0)];
        for (int i=0; i<values.length; i++){
            values[i] = itemList.getString(ITEM_KEY + i, "");
        }
    }

    private void initList() {
        for (String value : values) {
            Map<String, String> map = new HashMap<>();
            map.put(TEXT_KEY, value);
            map.put(NUM_OF_SYMBOL_KEY, String.valueOf(value.length()));
            content.add(map);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(DELETED_KEY, deletedIndexes);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        deletedIndexes = savedInstanceState.getIntegerArrayList(DELETED_KEY);
        for (int index : Objects.requireNonNull(deletedIndexes)){
            content.remove(index);
        }
        listSimpleAdapter.notifyDataSetChanged();
    }
}
