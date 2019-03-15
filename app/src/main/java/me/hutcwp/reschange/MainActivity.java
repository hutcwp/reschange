package me.hutcwp.reschange;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.text);
//        tv.setText(getString(R.string.res_change,Integer.toHexString(R.string.res_change)));
        String str = Integer.toHexString(R.string.res_change) + " : " + getString(R.string.res_change);
        tv.setText(str);
    }
}
