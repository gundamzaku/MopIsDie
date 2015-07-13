package com.tantanwen.mopisdie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;

import com.tantanwen.mopisdie.utils.Config;

import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageButton;
import pl.droidsonroids.gif.GifImageView;

public class Reply extends AppCompatActivity {

    private String[] f = {"0","1","2","3","4","5","6","7","8","9"};
    private Spinner face1,face2,face3;
    private CheckBox sig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        //获得所有的控件
        sig     = (CheckBox)findViewById(R.id.sig);    //是否签名
        face1   = (Spinner) findViewById(R.id.face1);
        face2   = (Spinner) findViewById(R.id.face2);
        face3   = (Spinner) findViewById(R.id.face3);

        initToolBar();
        initFaceSelect();
        initCheckBox();

        //检查
    }

    private void initToolBar(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle(R.string.title_activity_reply);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                System.out.println("我被点了");
                return false;
            }
        });
    }

    private void initCheckBox(){
        sig.setChecked(true);
    }

    private void initFaceSelect(){

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, f);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        face1.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, f);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        face2.setAdapter(adapter2);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, f);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        face3.setAdapter(adapter3);

        face1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bindFaceSelect();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        face2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bindFaceSelect();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        face3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bindFaceSelect();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }

    private void bindFaceSelect(){
        int face1Pos = face1.getSelectedItemPosition();
        int face2Pos = face2.getSelectedItemPosition();
        int face3Pos = face3.getSelectedItemPosition();

        GifImageView imageGifView = (GifImageView)findViewById(R.id.imageGifView);
        if(face1Pos==0 && face2Pos == 0 && face3Pos == 0){
            //没有图片
            imageGifView.setImageResource(0);
            imageGifView.setMinimumHeight(0);
        }else{
            InputStream is = null;

            try {
                GifDrawable gifFromAssets = new GifDrawable( getAssets(), "mop/"+f[face1Pos]+f[face2Pos]+f[face3Pos]+".gif" );
                imageGifView.setMinimumWidth(gifFromAssets.getMinimumWidth()*2);
                imageGifView.setMinimumHeight(gifFromAssets.getMinimumHeight()*2);
                imageGifView.setImageDrawable(gifFromAssets);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //拼合而成图片
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reply, menu);
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
