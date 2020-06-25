package com.example.studentmanager;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    ListView listView;
    List<String> items;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission denied! Asking for permission from user");
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
            }

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission denied! Asking for permission from user");
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 5678);
            }
        }
        items = new ArrayList<String>();
        listView = findViewById(R.id.list_view);

        listView.setLongClickable(true);
        registerForContextMenu(listView);
        findViewById(R.id.btn_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInsertCustomDialog();
            }
        });

        findViewById(R.id.btn_find).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFindCustomDialog();
            }
        });

        openDB();
        createTable();
        displayListView();
    }
    private void displayListView() {
        items.clear();

        String sql = "select * from studentTable";
        Cursor cs = db.rawQuery(sql, null);

        cs.moveToFirst();

        do {
            int ID = cs.getInt(0);
            String mssv = cs.getString(1);
            String name = cs.getString(cs.getColumnIndex("fullname"));
            String birthday = cs.getString(3);
            String email = cs.getString(4);
            String hometown = cs.getString(5);

            items.add( Integer.toString(ID) + "         " + mssv + "        " + name + "        " + email );
        }
        while (cs.moveToNext());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                items
        );

        listView.setAdapter(adapter);
    }

    private void openDB() {
        try {
            //String path = Environment.getExternalStorageDirectory() .getPath() + "/mydb"; //access SD Card
            String path = getFilesDir().getAbsolutePath() + "/mydb"; //access interal storage
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createTable() {
        db.beginTransaction();

        try {
            //create table and fields
            db.execSQL("create table studentTable(" +
                    "ID integer PRIMARY KEY autoincrement," +
                    "mssv text, "+
                    "fullname text," +
                    "birthday text," +
                    "email text," +
                    "hometown text)");

            //insert data
            db.execSQL("insert into studentTable(mssv, fullname, birthday, email, hometown) values('20160001', 'Le Hoang Anh', '22/03/1998', 'anh@gmail.com', 'Bac Giang')");
            db.execSQL("insert into studentTable(mssv, fullname, birthday, email, hometown) values('20160002', 'Nguyen Van Bac', '26/06/1998', 'bac@gmail.com', 'Ha Noi')");
            db.execSQL("insert into studentTable(mssv, fullname, birthday, email, hometown) values('20160003', 'Nguyen Viet Tien', '2/07/1998', 'tien@gmail.com', 'Bac Ninh')");

            db.setTransactionSuccessful();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }



    private void showFindCustomDialog() {
        final Dialog customDialog = new Dialog(MainActivity.this);
        customDialog.setContentView(R.layout.search);
        final EditText editText_querry = customDialog.findViewById(R.id.edit_text_find);
        final TextView textView = customDialog.findViewById(R.id.text_view_search_result);

        customDialog.findViewById(R.id.btn_find_student).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sql = "select * from studentTable where ( [mssv] like ('%" + editText_querry.getText().toString() + "%') or [fullname] like ('%" + editText_querry.getText().toString() + "%') )";
                Log.v("TAG", sql);
                Cursor cs = db.rawQuery(sql, null);

                cs.moveToFirst();

                String content = "";

                if (cs.getCount() > 0) {
                    do {
                        int ID = cs.getInt(0);
                        String mssv = cs.getString(cs.getColumnIndex("mssv"));
                        String fullname = cs.getString(2);
                        content += ( Integer.toString(ID) + "     " + mssv + "           " + fullname + "\n" );
                        Log.v("TAG", content);
                    }
                    while (cs.moveToNext());
                } else {
                    content = "result not found.";
                }

                textView.setText(content);
            }
        });

        customDialog.findViewById(R.id.btn_close2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });

        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();

        Window window = customDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); //set width, height for dialog
    }
    private void showInsertCustomDialog() {
        final Dialog customDialog = new Dialog(MainActivity.this);
        customDialog.setContentView(R.layout.insert);
        final EditText editText_mssv = customDialog.findViewById(R.id.edit_text_insert_mssv);
        final EditText editText_fullname = customDialog.findViewById(R.id.edit_text_inser_fullname);
        final EditText editText_birtday = customDialog.findViewById(R.id.edit_text_insert_birthday);
        final EditText editText_email = customDialog.findViewById(R.id.edit_text_insert_email);
        final EditText editText_hometown = customDialog.findViewById(R.id.edit_text_insert_hometown);

        customDialog.findViewById(R.id.btn_submit_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( editText_mssv.getText().toString().isEmpty() ||
                        editText_fullname.getText().toString().isEmpty() ||
                        editText_birtday.getText().toString().isEmpty() ||
                        editText_email.getText().toString().isEmpty() ||
                        editText_hometown.getText().toString().isEmpty() ) {

                    Toast.makeText(getApplicationContext(), "Please fill out all fields!", Toast.LENGTH_SHORT).show();

                } else {

                    db.beginTransaction();

                    try {
                        //insert data
                        db.execSQL("insert into studentTable(mssv, fullname, birthday, email, hometown) values( '" +
                                editText_mssv.getText().toString() + "', '" +
                                editText_fullname.getText().toString() + "', '" +
                                editText_birtday.getText().toString() + "', '" +
                                editText_email.getText().toString() + ".com', '" +
                                editText_hometown.getText().toString() +
                                "')");

                        db.setTransactionSuccessful();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        db.endTransaction();
                    }

                    customDialog.dismiss();

                    displayListView();

                }

            }
        });

        customDialog.findViewById(R.id.btn_close1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });

        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();

        Window window = customDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); //set width, height for dialog
    }
    private void showUpdateCustomDialog(final String table_id) {
        final Dialog customDialog = new Dialog(MainActivity.this);
        customDialog.setContentView(R.layout.update);
        final Spinner spinner = customDialog.findViewById(R.id.spinner);
        final EditText editText_update_content = customDialog.findViewById(R.id.edit_text_update_content);
        final TextView textView_update_result = customDialog.findViewById(R.id.text_view_update_result);

        //show content of seleted row
        String sql = "select * from studentTable where ID = '" + table_id + "'";
        Cursor cs = db.rawQuery(sql, null);

        cs.moveToFirst();
        textView_update_result.setText(cs.getInt(0) + "     " + cs.getString(1) + "     " + cs.getString(2) + "     " + cs.getString(3) + "     " + cs.getString(4) + "     " + cs.getString(5));

        //setup Spinner
        final List<String> spinner_items = new ArrayList<String>();
        spinner_items.add("mssv");
        spinner_items.add("fullname");
        spinner_items.add("birthday");
        spinner_items.add("email");
        spinner_items.add("hometown");

        ArrayAdapter adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                spinner_items
        );

        spinner.setAdapter(adapter);
        spinner.setSelection(0); // set selected item to default
        spinner.getSelectedItemPosition(); // get position of selected item

        customDialog.findViewById(R.id.btn_submit_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String update_content = editText_update_content.getText().toString();

                if (update_content.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill out edit text", Toast.LENGTH_SHORT).show();
                } else {
                    String sql = "update studentTable set " + spinner_items.get(spinner.getSelectedItemPosition()) + " = '" + update_content +"' where ID = '" + table_id + "'";
                    //Log.v("TAG", sql);
                    db.execSQL(sql);

                    //show content of seleted row
                    String sql2 = "select * from studentTable where ID = '" + table_id + "'";
                    Cursor cs = db.rawQuery(sql2, null);

                    cs.moveToFirst();
                    textView_update_result.setText(cs.getInt(0) + "     " + cs.getString(1) + "     " + cs.getString(2) + "     " + cs.getString(3) + "     " + cs.getString(4) + "     " + cs.getString(5));

                }

            }
        });

        customDialog.findViewById(R.id.btn_close3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
                displayListView();
            }
        });

        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();

        Window window = customDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); //set width, height for dialog
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle("Select an action");
        menu.add(0, 0, 0, "Update");
        menu.add(0, 1, 0, "Remove");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Log.v("TAG", items.get(info.position) + " selected");

        int item_id = info.position; //get id of item in listview
        String item_content = items.get(item_id); //get content of selected item
        String table_id = item_content.substring(0, item_content.indexOf(" ") ); //get table id of selected item
        Log.v("TAG", table_id);

        int id = item.getItemId(); // get id of item in contextMenu

        if (id == 0) { // Update
            //Log.v("TAG", "Update action");
            showUpdateCustomDialog(table_id);
        } else if (id == 1) { // Remove
            //Log.v("TAG", "Remove action");
            String sql = "delete from studentTable where ID = '"+ table_id +"'";
            //Log.v("TAG", sql);
            db.execSQL(sql); //excute sql to remove row

            displayListView(); //re dislpay listview
        }

        return super.onContextItemSelected(item);
    }
}
