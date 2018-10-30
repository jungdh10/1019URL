package com.example.a503_14.a1019url;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.os.*;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Stream;

public class ImageDownload extends AppCompatActivity {
    ImageView imageView;
    Button display,download;

    //bitmap을 imageview에 출력하는 핸들러
    Handler displayHandler=new Handler(){
        @Override
        public void handleMessage(Message message){
            Bitmap bitmap=(Bitmap)message.obj;
            imageView.setImageBitmap(bitmap);
        }
    };

    Handler downloadHandler=new Handler(){
        @Override
        public void handleMessage(Message message){
            //파일이 존재하는 경우
            if(message.obj!=null){
                //Android의 Data 디렉토리 경로를 가져오기
                String path=Environment.getDataDirectory().getAbsolutePath();
                //현재 앱 내의 파일 경로 만들기
                path=path+"com.example.a503_14.a1019url/files"+(String)message.obj;
                //이미지 파일을 imageView에 출력
                imageView.setImageBitmap(BitmapFactory.decodeFile(path));
            }
            //파일이 존재하지 않는 경우
            else{
                Toast.makeText(ImageDownload.this, "파일이 존재하지 읺습니다", Toast.LENGTH_LONG).show();
            }
        }
    };

    //"바로출력"버튼을 누르면 웹의 이미지가 바로출력
    class DisplayThread extends Thread{
        @Override
        public void run(){
            try{
                //이미지 가져올 주소
                String addr="https://www.fmkorea.com/files/attach/new/20151231/66955397/106450328/282722072/1fa11912e7f1eee5bb6c302f94127909.jpg";
                URL url= new URL(addr);
                //url에 연결해서 비트맵 만들기
                Bitmap bitmap= BitmapFactory.decodeStream(url.openStream());
                //bitmap을 핸들러에게 전송
                Message message=new Message();
                message.obj=bitmap;
                displayHandler.sendMessage(message);
            }catch(Exception e){
                Log.e("이미지 가져오기", e.getMessage());
            }
        }
    }

    //이미지를 다운로드 받아서 파일로 저장하는 스레드
    class DownloadThread extends Thread{
        //넘겨받기 위한 변수 생성
        String addr;
        String filepath;

        public DownloadThread(String addr, String filepath){
            this.addr=addr;
            this.filepath=filepath;
        }
        @Override
        public void run() {
            try {
                URL url=new URL(addr);
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                con.setUseCaches(false);
                con.setConnectTimeout(20000);

                //내용을 읽을 스트림을 생성
                InputStream is=con.getInputStream();
                //기록할 스트림을 생성
                PrintStream pw=new PrintStream(openFileOutput(filepath, 0));
                //is에서 읽어서 pw에 기록
                while(true){
                    byte [] b= new byte[1024];
                    int read=is.read(b);
                    if(read <=0) break;
                    pw.write(b,0,read);
                }
                is.close();
                pw.close();
                con.disconnect();
                Message message=new Message();
                message.obj=filepath;
                downloadHandler.sendMessage(message);

            }
            catch (Exception e) {
                Log.e("이미지 다운로드 실패", e.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_download);

        imageView=(ImageView)findViewById(R.id.imageView);
        display=(Button)findViewById(R.id.display);
        download=(Button)findViewById(R.id.download);

        display.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DisplayThread th=new DisplayThread();
                th.start();
            }
        });

        download.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //이미지를 다운로드 받을 주소
                String imageUrl="https://mblogthumb-phinf.pstatic.net/MjAxODA1MjFfMjkz/MDAxNTI2ODkyMzc1OTg2.d4YNA0u7tr4Oaw03Qfxg8LOCE2jsCfTzaLOzy8wC-a4g.uhUp2ZWlkrKgxDkpj5h5_IrnqhYLr8QBLAmePDYJ3x0g.JPEG.namil79/20180518_164242_edit.jpg?type=w2";
                //파일명 만들기
                int idx=imageUrl.lastIndexOf("/");
                String filename=imageUrl.substring(idx+1);
                //파일 경로 만들기
                String data=Environment.getDataDirectory().getAbsolutePath();
                String path=data+"/data/com.example.a503_14.a1019url/files/"+filename;
                //파일 존재 여부를 확인
                if(new File(path).exists()){
                    Toast.makeText(ImageDownload.this, "파일이 존재합니다", Toast.LENGTH_LONG).show();
                    imageView.setImageBitmap(BitmapFactory.decodeFile(path));
                }else{
                    Toast.makeText(ImageDownload.this, "파일이 존재하지 않습니다", Toast.LENGTH_LONG).show();
                    //넘겨준 이미지 파일의 경로와 저장할 파일 경로 확인
                    Log.e("이미지 파일 경로", imageUrl);
                    Log.e("저장할 파일 경로", path);

                    //(이미지주소, 파일이름)
                    DownloadThread th=new DownloadThread(imageUrl, filename);
                    th.start();
                }

            }
        });


    }
}
