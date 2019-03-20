package com.limiao.annotationdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.limiao.ioc_annotation.BindView;
import com.limiao.ioc_annotation.ContentView;
import com.limiao.ioc_api.ViewInjector;

@ContentView(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv)
    TextView mTextView;
    @BindView(R.id.iv)
    ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        ViewInjector.injectView(this);
        mTextView.setText("aaaaaaaa");
        mImageView.setImageResource(R.mipmap.ic_launcher);
    }
}
