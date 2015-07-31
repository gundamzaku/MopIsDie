package com.tantanwen.mopisdie;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tantanwen.mopisdie.module.SearchCondition;

public class Search extends AppCompatActivity {

    public static boolean isLock = false; //锁
    private Button buttonSearch;
    private Button buttonMySend;
    private Button buttonMyReply;
    private EditText keyword;
    private Spinner searchtype;
    private SearchCondition search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //收取控件
        buttonSearch = (Button)findViewById(R.id.button_search);
        buttonMySend = (Button)findViewById(R.id.button_my_send);
        buttonMyReply = (Button)findViewById(R.id.button_my_reply);

        keyword = (EditText)findViewById(R.id.keyword);
        searchtype = (Spinner)findViewById(R.id.searchtype);
        search = new SearchCondition(mHandler);
        //绑定查询
        bindSearch();
        //绑定自发
        bindMySend();
        //绑定自回
        bindMyReply();
    }

    private void bindSearch(){
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isLock == true){
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.repeat_submit), Toast.LENGTH_SHORT).show();
                    return;
                }
                //启动线程
                System.out.println(searchtype.getSelectedItemId());
                search.setType(1);
                search.setKeyword(String.valueOf(keyword.getText()));
                Resources res =getResources();
                String[] searchTypes = res.getStringArray(R.array.search_types);
                System.out.println(searchTypes[searchtype.getSelectedItemPosition()]);
                search.setSearchType(searchTypes[searchtype.getSelectedItemPosition()]);
                Thread td = new Thread(search);
                td.start();
            }
        });
    }

    private void bindMyReply(){
        //查我自己的回复
        buttonMyReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isLock == true) {
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.repeat_submit), Toast.LENGTH_SHORT).show();
                    return;
                }
                //启动线程
                search.setType(2);
                Thread td = new Thread(search);
                td.start();
            }
        });
    }
    private void bindMySend(){
        //查我自己的帖子
        buttonMySend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isLock == true){
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.repeat_submit), Toast.LENGTH_SHORT).show();
                    return;
                }
                //启动线程
                search.setType(3);
                Thread td = new Thread(search);
                td.start();
            }
        });
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {//此方法在ui线程运行
            System.out.print("结束线程");
            search.unLockStatus();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
