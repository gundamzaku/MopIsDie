package com.tantanwen.mopisdie;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.tantanwen.mopisdie.adapter.PmMessageAdapter;
import com.tantanwen.mopisdie.adapter.PmMessageContainer;
import com.tantanwen.mopisdie.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class PmMessage extends AppCompatActivity {

    private Button mBtnSend;
    private EditText mEditTextContent;
    //聊天内容的适配器
    private PmMessageAdapter mAdapter;
    private ListView mListView;
    //聊天的内容
    private List<PmMessageContainer> mDataArrays = new ArrayList<PmMessageContainer>();

    /*
    private String[] msgArray = new String[]{"  孩子们，要好好学习，天天向上！要好好听课，不要翘课！不要挂科，多拿奖学金！三等奖学金的争取拿二等，二等的争取拿一等，一等的争取拿励志！",
            "姚妈妈还有什么吩咐...",
            "还有，明天早上记得跑操啊，不来的就扣德育分！",
            "德育分是什么？扣了会怎么样？",
            "德育分会影响奖学金评比，严重的话，会影响毕业",
            "哇！学院那么不人道？",
            "你要是你不听话，我当场让你不能毕业！",
            "姚妈妈，我知错了(- -我错在哪了...)"};

    private String[]dataArray = new String[]{"2012-09-01 18:00", "2012-09-01 18:10",
            "2012-09-01 18:11", "2012-09-01 18:20",
            "2012-09-01 18:30", "2012-09-01 18:35",
            "2012-09-01 18:40", "2012-09-01 18:50"};
    */

    private String[] msgArray;
    private String[] dataArray;

    private String userNick;
    private String re;
    private String content;
    private int pmid;
    private String sendTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm_message);

        Intent intent =getIntent();

        userNick    = intent.getStringExtra("userNick");
        re           = intent.getStringExtra("re");
        content     = intent.getStringExtra("content");
        pmid         = intent.getIntExtra("pmid", 0);
        sendTime    = intent.getStringExtra("sendTime");
        System.out.println(userNick);
        System.out.println(re);
        System.out.println(content);
        System.out.println(pmid);
        System.out.println(sendTime);

        initView();
        initData();
    }
    //初始化视图
    private void initView() {
        mListView = (ListView) findViewById(R.id.listview);

        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送
                send();
            }
        });
        mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
    }

    //初始化要显示的数据
    private void initData() {

        PmMessageContainer entity;

        if(re.length()>0){
            entity = new PmMessageContainer();
            entity.setDate(sendTime);
            entity.setText(re);
            entity.setName("你自己");
            entity.setMsgType(false);
            mDataArrays.add(entity);
        }

        if(content.length()>0){
            entity = new PmMessageContainer();
            entity.setDate(sendTime);
            entity.setText(content);
            entity.setName(userNick);
            entity.setMsgType(true);
            mDataArrays.add(entity);
        }

        mAdapter = new PmMessageAdapter(this, mDataArrays);
        mListView.setAdapter(mAdapter);

    }

    private void send(){

        String contString = mEditTextContent.getText().toString();
        if (contString.length() > 0) {
            PmMessageContainer entity = new PmMessageContainer();
            entity.setDate(Utils.getDate());
            entity.setName("");
            entity.setMsgType(false);
            entity.setText(contString);
            mDataArrays.add(entity);
            mAdapter.notifyDataSetChanged();
            mEditTextContent.setText("");
            mListView.setSelection(mListView.getCount() - 1);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pm_message, menu);
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
