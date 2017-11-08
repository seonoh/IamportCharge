package com.seonohpro.iamportchargetest;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URISyntaxException;

public class ChargeActivity extends AppCompatActivity {


    WebView webView;
    String itemName;
    int itemAmount;
    boolean isdChargeFinsh;     // 결제 정보를 넘기는 타이밍
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);
        itemName = getIntent().getStringExtra("itemName");
        itemAmount = getIntent().getIntExtra("itemAmount",0);


        if(TextUtils.isEmpty(itemName) || itemAmount<=0){
            Intent intent = new Intent();
            intent.putExtra("err","결제 정보가 부정확 합니다.");
            // 액티비티에 결과 돌려주기
            setResult(MainActivity.RESPONSE_CHAARGE_CODE,intent);
            finish();
        }
        webView = (WebView)findViewById(R.id.webView);
        initWeView();


    }
    public void initWeView(){
        // 자바 스크립트 활성화
        webView.getSettings().setJavaScriptEnabled(true);
        // 결제된 이전 정보가 나오면 안되기 때문에 캐싱하지 않음
        webView.getSettings().setAppCacheEnabled(false);
        // 웹브라우저 데이터베이스 사용
        webView.getSettings().setDatabaseEnabled(true);
        String path = getApplication().getDir("webStorage", Context.MODE_PRIVATE).getPath();
        webView.getSettings().setDatabasePath(path);
        // DOM 처리
        webView.getSettings().setDomStorageEnabled(true);
        // 입력을 저장 처리 안함
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
        // 엑세스
        webView.getSettings().setAllowContentAccess(false);
        // 윈도우 오픈
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(true);

        // 자바와 통신하는 인터페이스
        webView.addJavascriptInterface(new MyInter(), "pay");
        // 크롬 클라이언트
        webView.setWebChromeClient(new WebChromeClient(){

            // 메소드 내부에서 재정의 하여 팝업 자체 상단에 보이는 URL을 숨긴다 ============================
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            // 웹 페이지 로딩의 진행률 (바 형태로 홤녀을 셋팅할 때 유용함)
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(newProgress == 100 && !isdChargeFinsh){

                    isdChargeFinsh = true;
                    // 결제 정보 전달 (java -> html(JavaScript)에게 데이터 전달)
                    webView.loadUrl("javascript:paynow('"+itemName+"',"+itemAmount+");");

                }
            }
            // 자바스크립트의 로그 : console.log("sadassad");

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("T",""+consoleMessage);
                return super.onConsoleMessage(consoleMessage);
            }


            // ====================================================================================
        });

        // 웹 클라이언트
        webView.setWebViewClient(new WebViewClient(){
            // 웹 페이지가 시작되면 호출
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // 로딩 시작

            }

            // 로딩이 끝나면 호출출
           @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
               // 로딩 끝
               String check = "http://13.124.94.151:3000";
               /*if(url.indexOf(check)==0){
                   goback(url.substring(check.length(), url.length()));
               }*/
            }

            // url이 작동되면 여기서 먼저 체크 => 스키마 (xxx://...) {xxx}에 따라 다르게 작동하도록 조치함
            // 결제 앱이나 모듈이 없을 경우 설치로 유도해야 하기 때문이다.
            // iamport에서 제공
            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
                    //3rd-party앱에 대한 URL scheme 대응
                    Intent intent = null;

                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME); //IntentURI처리
                        Uri uri = Uri.parse(intent.getDataString());

                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        return true;
                    } catch (URISyntaxException ex) {
                        return false;
                    } catch (ActivityNotFoundException e) {
                        if (intent == null) return false;

                        //설치되지 않은 앱에 대해 market이동 처리
//                        if ( handleNotFoundPaymentScheme(intent.getScheme()) )	return true;

                        //handleNotFoundPaymentScheme()에서 처리되지 않은 것 중, url로부터 package정보를 추출할 수 있는 경우 market이동 처리
                        String packageName = intent.getPackage();
                        if (packageName != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            return true;
                        }

                        return false;
                    }
                }
//                super.shouldOverrideUrlLoading(WebView view, String url);
                return false;
            }
        });


        // 결제 페이지 로딩
        // 하이브리드앱
        webView.loadUrl("file:///android_asset/index.html");  //asset까지만 쓴다. (원래 assets인데)
//        webView.loadUrl("http://m.naver.com");  //asset까지만 쓴다. (원래 assets인데)



    }
    public class MyInter{


        // javaScript에서 java로 전달
        @JavascriptInterface
        public void showResult(String msg){

            // 웹페이지에서 결제가 모두 끝나면 호출된다 -> MainActivity로 전달
            Intent intent = new Intent();
            intent.putExtra("suc",msg);
            // 액티비티에 결과 돌려주기
            setResult(MainActivity.RESPONSE_CHAARGE_CODE,intent);
            finish();
        }
    }
    // 결제 앱에서 내 앱으로 돌아오게 하는 처리
    private final String APP_SCHEME = "iamporttest://"; //AndroidManifest.xml에서 정의한 것과 동일한 URL scheme사용(substring을 위한 용도)

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri intentData = intent.getData();

        if ( intentData != null ) {
            //isp 인증 후 복귀했을 때 결제 후속조치
            String url = intentData.toString();
            Log.e("SEONOH",url);
            if ( url.startsWith(APP_SCHEME) ) {
                //My앱의 WebView가 표시해야 할 웹 컨텐츠의 주소가 전달됩니다.
                String redirectURL = url.substring(APP_SCHEME.length()+3);
                webView.loadUrl(redirectURL);
            }
        }
    }
}
