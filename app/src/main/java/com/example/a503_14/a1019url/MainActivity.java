package com.example.a503_14.a1019url;

import android.app.ProgressDialog;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//문자열 받기
public class MainActivity extends AppCompatActivity {
    EditText url;
    Button download;
    TextView html;
    ProgressDialog progressDialog;

    //데이터를 출력할 핸들러 만들기
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message){
            Log.e("핸들러 호출","핸들러가 호출되었습니다.");
            //스레드가 넘겨준 데이터를 텍스트 뷰에 출력(데이터는 obj에 넘겨줌)
            html.setText(message.obj.toString());
            progressDialog.dismiss();
        }
    };
    /*
    //클래스를 만들고(인스턴스 객체가 하나만 만들어짐-한번만 쓸것이면 이렇게 생성)
    Thread th =new Thread(){ };
    */

    //여러번 호출해야 하므로 클래스를 만들고 나중에 객체 생성
    //네트워크하려면 try,catch는 해줘야 할 것
    class ThreadEx extends Thread{
        @Override
        public void run(){
            try{
                Log.e("스레드", "시작");
                //다운로드 받을 주소 가져오기
                String addr = url.getText().toString();
                Log.e("다운로드 받을 주소", addr);
                //문자열 주소로 URL 객체 생성
                URL downloadUrl = new URL(addr);
                //연결 객체 생성
                HttpURLConnection con = (HttpURLConnection)downloadUrl.openConnection();
                //옵션 설정
                con.setConnectTimeout(20000);
                con.setUseCaches(false);
                Log.e("connection", con.toString());
                //문자열 다운로드 받기 위한 스트림 생성
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                //줄 단위로 문자열을 읽어서 sb에 추가
                StringBuilder sb = new StringBuilder();
                while(true){
                    String line = br.readLine();
                    //Log.e("읽은 내용", line);
                    if(line == null)
                        break;
                    sb.append(line + "\n");
                }
                //전부 가져왔으면 닫기
                br.close();
                con.disconnect();
                //Message에 저장해서 handler에게 메시지 전송
                Message message = new Message();
                message.obj = sb.toString();
                handler.sendMessage(message);
            }catch(Exception e){
                Log.e("다운로드 에러", e.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        url = (EditText)findViewById(R.id.url);
        download = (Button)findViewById(R.id.download);
        html = (TextView)findViewById(R.id.html);

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //이 구문이 출력이 안되면 버튼의 아이디를 확인
                //Log.e("버튼 클릭", "클릭 이벤트를 수행합니다.");
                progressDialog= ProgressDialog.show(MainActivity.this, "", "다운로드 중...");
                ThreadEx th = new ThreadEx();
                th.start();
            }
        });
    }
}