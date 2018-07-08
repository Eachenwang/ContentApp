package com.eth.contentapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import static android.Manifest.permission.*;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //檢查有無獲得權限並得參數
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        //若無參數為無獲得權限， 則向使用者請求權限
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{READ_CONTACTS, WRITE_CONTACTS},
                    REQUEST_CONTACTS);
        } else {
            //已有權限則進行檔案存取
            readContacts();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得聯絡人權限
                    readContacts();
                }else {
                    //使用者拒絕 顯示對話框告知
                    new AlertDialog.Builder(this)
                            .setMessage("需要允許聯絡人權限才能顯示")
                            .setPositiveButton("ok", null)
                            .show();
                }
                return;
        }
    }

    private void readContacts() {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
        );

//        while (cursor.moveToNext()) {
//            //處理每一筆資料
//            int id = cursor.getInt(cursor.getColumnIndex(
//                    ContactsContract.Contacts._ID));
//            String name = cursor.getString(cursor.getColumnIndex(
//                    ContactsContract.Contacts.DISPLAY_NAME));
//            Log.d("RECORD", "readContacts: "+id+"/"+name);
//        }

        ListView list = findViewById(R.id.list);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                new int[]{android.R.id.text1},
                1);
        list.setAdapter(adapter);
    }
}
