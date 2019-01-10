package com.threedi.simulatewritevideofile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public static final String SOURCE_FILE_DIR = "/sdcard/";
    public static final String DEST_FILE_DIR ="/sdcard/testplay/";
    public int currentIndex = 0;

    public static Uri mTreeUri = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void directCopy(View v){
        Log.e("test",mTreeUri.getPath());
        DocumentFile file = DocumentFile.fromTreeUri(this,mTreeUri);
        DocumentFile newfile = file.createFile("video/mp4",currentIndex+".mp4");
        copy(new File(SOURCE_FILE_DIR+currentIndex+".mp4"),newfile,this);
    }

    public void selectPath(View v){
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.setFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        startActivityForResult(i,12);
    }

    public  boolean copy(File copy, DocumentFile file, Context con){
         FileInputStream inStream = null;
         OutputStream outStream = null;
        try {
            inStream = new FileInputStream(copy);
            outStream =
                    con.getContentResolver().openOutputStream(file.getUri());
            byte[] buffer = new byte[16384];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            currentIndex++;
            Toast.makeText(this,"the "+currentIndex+"th video copy finished!",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(inStream !=null){
                    inStream.close();
                }
                if(outStream != null){
                    outStream.close();
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mTreeUri = data.getData();

        //设置Uri访问权限
        final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION|
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        getContentResolver().takePersistableUriPermission(mTreeUri,takeFlags);

        //通过Uri 使用DocumentFile 进行文件操作。
        DocumentFile file = DocumentFile.fromTreeUri(this,data.getData());
        DocumentFile newfile = file.createFile("video/mp4",currentIndex+".mp4");

        //标准文件 拷贝到 SAF Uri
        copy(new File("/sdcard/0.mp4"),newfile,this);

    }
}
