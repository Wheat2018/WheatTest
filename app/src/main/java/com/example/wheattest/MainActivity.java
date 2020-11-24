package com.example.wheattest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private LinearLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentLayout = findViewById(R.id.linearLayout);
        Log.e("onCreate", "MainActivity:" + this.toString() +
                " on Thread " + Thread.currentThread().getId());
        new Thread(() -> {
            JSONArray jsonArray = Utils.fileToJSONArray(getResources(), "metadata.json");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        Announce announce = createAnnounce((JSONObject) jsonArray.get(i));
                        Object[] obj = { MainActivity.this, announce};
                        Message msg = new Message();
                        msg.obj = obj;
                        handler.sendMessage(msg);
                        // addAnnounce((JSONObject) jsonArray.get(i));
                    } catch (JSONException e) {
                        Log.e("onCreate", e.toString());
                    }
                }
            }
        }).start();
//        findViewById(R.id.scrollView).setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                Log.e("onScrollChange", String.format("%d, %d, %d, %d", scrollX, scrollY, oldScrollX, oldScrollY));
//            }
//        });
    }

    private static final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            Object[] obj = (Object[]) msg.obj;
            MainActivity main = (MainActivity) obj[0];
            Announce announce = (Announce) obj[1];
            main.addAnnounce(announce);
            super.handleMessage(msg);
        }
    };

    public Announce createAnnounce(JSONObject values){
        int type = 0;
        try {
            type = values.getInt("type");
        } catch (JSONException e) {
            Log.e("addAnnounce", "Cannot determine type of the announce. Use default type(" + type + ")");
        }
        Announce announce = AnnounceFactory.createAnnounce(this, type);
        if (announce != null) {
            announce.setLayoutParams(Announce.params(Announce.match_parent, Announce.wrap_content, 0, null));
            announce.SetValues(values);
            announce.mainActivity = this;
        }
        else
            Log.e("addAnnounce", "Unsupported announce type id:" + type);
        return announce;
    }

    public void addAnnounce(Announce announce) {
        if (announce != null)
            contentLayout.addView(announce);
    }

    public int px(float dp) {
        final float scale = getResources().getDisplayMetrics().density; //当前屏幕密度因子
        return (int)(dp * scale + 0.5f);
    }
    public int dp(float px) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
}
