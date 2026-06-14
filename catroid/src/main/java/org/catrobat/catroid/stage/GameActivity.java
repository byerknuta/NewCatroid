package org.catrobat.catroid.stage;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.danvexteam.lunoscript_annotations.LunoClass;

import org.catrobat.catroid.R;

@LunoClass
public class GameActivity extends AppCompatActivity {
    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        myWebView = findViewById(R.id.myWebView);
        myWebView.setWebViewClient(new WebViewClient());

        myWebView.getLayoutParams().width = 300;
        myWebView.getLayoutParams().height = 400;
        myWebView.requestLayout();
        myWebView.loadUrl("https://uchi.ru/");
    }

    @Override
    protected void onDestroy() {
        if (myWebView != null) {
            myWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

}
