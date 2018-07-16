package com.eth.contentapp;

import android.Manifest;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.content.ContentProvider;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.widget.TextView;

import java.util.ArrayList;

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
        Cursor cursor = resolver.query(Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);

//        while (cursor.moveToNext()) {
//            //處理每一筆資料
//            int id = cursor.getInt(cursor.getColumnIndex(
//                    ContactsContract.Contacts._ID));
//            String name = cursor.getString(cursor.getColumnIndex(
//                    ContactsContract.Contacts.DISPLAY_NAME));
//            Log.d("RECORD", "readContacts: "+id+"/"+name);
//        }
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[]{Contacts.DISPLAY_NAME,
                        Contacts.HAS_PHONE_NUMBER},
                new int[]{android.R.id.text1,
                        android.R.id.text2},
                1) {
            //客製化
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);
                TextView phone = view.findViewById(android.R.id.text2);
                if (cursor.getInt(cursor.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) == 0) {
                    phone.setText("");
                } else {
                    int id = cursor.getInt(cursor.getColumnIndex(Contacts._ID));
                    Cursor pCursor = getContentResolver().query(
                            Phone.CONTENT_URI,
                            null,
                            Phone.CONTACT_ID+"=?",
                            new String[]{String.valueOf(id)},
                            null);
                    if (pCursor.moveToFirst()) {
                        String number = pCursor.getString(pCursor.getColumnIndex(
                                Phone.DATA));
                        phone.setText(number);
                    }
                }
            }
        };
        ListView list = findViewById(R.id.list);
        list.setAdapter(adapter);
    }

    public void insertContact(View view){
        ArrayList ops = new ArrayList();
        int index = ops.size();
        ops.add(ContentProviderOperation
                .newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, index)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, "jane").build());
        ops.add(ContentProviderOperation
                .newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, index)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, "0900112233")
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE).build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void updateContact(View view) {
        String where = Phone.DISPLAY_NAME + " = ? AND "+Data.MIMETYPE+ "= ?";
        String[] params = new String[]{"jane", Phone.CONTENT_ITEM_TYPE};
        ArrayList ops = new ArrayList();
        ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                .withSelection(where, params)
                .withValue(Phone.NUMBER, "0900333333")
                .build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void deleteContact(View view){
        String where = Data.DISPLAY_NAME + "= ?";
        String[] params = new String[]{"jane"};
        ArrayList ops = new ArrayList();
        ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
                .withSelection(where, params)
                .build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
