package com.seonohpro.iamportchargetest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    static int REQUEST_CHAARGE_CODE = 1000;        // 결제 요청 코드
    static int RESPONSE_CHAARGE_CODE = 1001;       // 결제 응답 코드
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    // 결제 시작
    public void onCharge(View view){
        // 결제 시작
        Intent intent = new Intent(this, ChargeActivity.class);
        intent.putExtra("itemName","델몬트 MIX");
        intent.putExtra("itemAmount",1004);
        //결과를 돌려 받기위해 startActivityForResult 사용
        startActivityForResult(intent,REQUEST_CHAARGE_CODE);

    }

    // 결과를 돌려 받는다
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(REQUEST_CHAARGE_CODE == requestCode){
            if(resultCode == RESPONSE_CHAARGE_CODE){
                //성공 혹은 결제 결과를 받음
                Toast.makeText(this, "결제 결과 "+data.getSerializableExtra("suc"), Toast.LENGTH_SHORT).show();
            }else{
                //오류
                Toast.makeText(this, "결제 결과 "+data.getSerializableExtra("err"), Toast.LENGTH_SHORT).show();

            }
        }
    }
}
