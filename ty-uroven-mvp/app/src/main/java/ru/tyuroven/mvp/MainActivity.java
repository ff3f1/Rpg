package ru.tyuroven.mvp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import android.graphics.Color;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private static final int PICK_JSON=44;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(19,17,28));
        getWindow().setNavigationBarColor(Color.rgb(29,24,40));
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(19,17,28));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.getSettings().setDefaultTextEncodingName("UTF-8");
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if(fileCallback!=null)fileCallback.onReceiveValue(null);
                fileCallback=callback;
                try{
                    Intent i=new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("application/json");
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(i,"Импорт резервной копии"),PICK_JSON);
                    return true;
                }catch(Exception e){fileCallback=null;return false;}
            }
        });
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface public void exportJson(String json) {
                runOnUiThread(() -> {
                    Intent i=new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT,json);
                    startActivity(Intent.createChooser(i,"Сохранить или отправить резервную копию"));
                });
            }
        }, "AndroidBridge");
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_JSON&&fileCallback!=null){
            Uri u=(resultCode==RESULT_OK&&data!=null)?data.getData():null;
            fileCallback.onReceiveValue(u==null?null:new Uri[]{u});
            fileCallback=null;
        }
    }
    @Override public void onBackPressed() {
        if (web!=null) web.evaluateJavascript("(function(){if(document.getElementById('modal').classList.contains('open'))closeModal();else navigate('home')})()",null);
        else super.onBackPressed();
    }
    @Override public void onDestroy() {
        if(web!=null){web.destroy();web=null;}
        super.onDestroy();
    }
}
