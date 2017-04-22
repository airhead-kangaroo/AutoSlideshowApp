package jp.techacademy.hideto.uetsuka.autoslideshowapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Uri> imageList;
    private int imageNumber;
    private int nowImage;
    private ImageView imageView;
    private Button nextBtn;
    private Button backBtn;
    private Button timerBtn;
    private boolean isTimerOn;
    private Timer mTimer;
    private Handler mHandler;
    private final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nowImage = 0;
        imageList = new ArrayList<>();
        initButton();
        checkPermission();
    }

    private void init(){
        initImageUri();
        setButtonState(true,true,true);
        isTimerOn = false;
        mHandler = new Handler();
        imageNumber = imageList.size();
        imageView = (ImageView)findViewById(R.id.imageArea);
        showImage();
    }

    private void initImageUri(){
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );
        //if(cursor.moveToFirst()){
        cursor.moveToFirst();
            do{
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                imageList.add(imageUri);
            }while(cursor.moveToNext());
        //}
    }

    private void checkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                init();
            }else{
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
            }
        }else{
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    Toast.makeText(this,"ファイル読み込みが許可されていません",Toast.LENGTH_LONG);
                    setButtonState(false,false,false);
                }
                break;
            default:
                break;
        }
    }

    private void initButton() {
        nextBtn = (Button) findViewById(R.id.nextBtn);
        backBtn = (Button) findViewById(R.id.backBtn);
        timerBtn = (Button) findViewById(R.id.timerBtn);
    }
    private void setButtonState(boolean nextBtnFlag, boolean backBtnFlag, boolean timerBtnFlag){
        nextBtn.setEnabled(nextBtnFlag);
        backBtn.setEnabled(backBtnFlag);
        timerBtn.setEnabled(timerBtnFlag);
    }

    private void showImage(){
        imageView.setImageURI(imageList.get(nowImage));
    }

    private void setNextImage(){
        nowImage += 1;
        nowImage %= imageNumber;
        showImage();
    }

    public void nextBtn(View v){
        setNextImage();
    }

    public void backBtn(View v){
        nowImage -= 1;
        if(nowImage < 0){
            nowImage = imageNumber - 1;
        }
        showImage();
    }

    public void timerBtn(View v){
        if(!isTimerOn){
            isTimerOn = true;
            timerBtn.setText(R.string.timerStop);
            nextBtn.setEnabled(false);
            backBtn.setEnabled(false);
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setNextImage();
                        }
                    });
                }
            },2000,2000);
        }else{
            isTimerOn = false;
            mTimer.cancel();
            nextBtn.setEnabled(true);
            backBtn.setEnabled(true);
        }
    }


}
