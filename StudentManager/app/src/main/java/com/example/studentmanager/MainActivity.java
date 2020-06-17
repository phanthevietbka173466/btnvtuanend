package com.example.studentmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Database database;
    List<Student> studentList;
    RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        studentList = new ArrayList<>();

        //tao database
        database = new Database(this,"student.sqlite",null,1);
        //tao bang
        database.QueryData("CREATE TABLE IF NOT EXISTS Student(Mssv INTEGER PRIMARY KEY , HoTen VARCHAR(100) , NgaySinh VARCHAR(100), Email VARCHAR(100) , QueQuan VARCHAR(100) );");

        // lay du lieu lan dau
        getDataFromSQL();
    }

    //tao menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getDataFromSQLBySearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() < 1) {
                    getDataFromSQL();
                }
                else {
                    getDataFromSQLBySearch(newText);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    //bat su kien an item tren menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            dialogAddSV();
        }
        return super.onOptionsItemSelected(item);
    }

    //dialog them sinh vien
    private void dialogAddSV() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_sv);
        // init cac widget trong dialog
        final EditText editMssv = dialog.findViewById(R.id.edit_mssv);
        final EditText editHoTen = dialog.findViewById(R.id.edit_hoTen);
        final EditText editNgaySinh = dialog.findViewById(R.id.edit_ngaySinh);
        final EditText editEmail = dialog.findViewById(R.id.edit_email);
        final EditText editQueQuan = dialog.findViewById(R.id.edit_queQuan);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_save = dialog.findViewById(R.id.btn_save);

        //format ngay sinh theo dung dd/mm/yyyy
        editNgaySinh.addTextChangedListener(new TextWatcher(){
            private String current = "";
            private String ddmmyyyy = "ddmmyyyy";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]", "");
                    String cleanC = current.replaceAll("[^\\d.]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }else{
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        if(mon > 12) mon = 12;
                        cal.set(Calendar.MONTH, mon-1);

                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    editNgaySinh.setText(current);
                    editNgaySinh.setSelection(sel < current.length() ? sel : current.length());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mssv = editMssv.getText().toString().trim();
                String hoTen = editHoTen.getText().toString().trim();
                String ngaySinh = editNgaySinh.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String queQuan = editQueQuan.getText().toString().trim();
                if (mssv.equals("") || hoTen.equals("") || ngaySinh.equals("") || email.equals("") || queQuan.equals("")) {
                    dialogMessage("Thông tin còn thiếu!");
                }
                else {
                    Cursor tmp = database.GetData("SELECT * FROM Student WHERE Mssv="+Integer.parseInt(mssv)+" ");
                    if( !tmp.moveToFirst()) {
                        database.QueryData("INSERT INTO Student VALUES ("+mssv+",'"+hoTen+"','"+ngaySinh+"','"+email+"','"+queQuan+"')");
                        dialogMessage("Thêm sinh viên thành công");
                        dialog.cancel();
                        getDataFromSQL();
                    }
                    else {
                        dialogMessage("Mã sinh viên đã tồn tại");
                    }
                }
            }
        });

        dialog.show();
    }

    //dialog edit sinh vien
    public void dialogEditSV(Student student) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_sv);
        final EditText editMssv = dialog.findViewById(R.id.edit_mssv);
        final EditText editHoTen = dialog.findViewById(R.id.edit_hoTen);
        final EditText editNgaySinh = dialog.findViewById(R.id.edit_ngaySinh);
        final EditText editEmail = dialog.findViewById(R.id.edit_email);
        final EditText editQueQuan = dialog.findViewById(R.id.edit_queQuan);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_save = dialog.findViewById(R.id.btn_save);

        //dien san thong tin vao cac thuoc tinh
        editMssv.setText(student.getMssv()+"");
        editMssv.setEnabled(false);
        editHoTen.setText(student.getHoTen());
        editNgaySinh.setText(student.getNgaySinh());
        editEmail.setText(student.getEmail());
        editQueQuan.setText(student.getQueQuan());

        //format ngay sinh nhap vao theo dung dd/mm/yyyy
        editNgaySinh.addTextChangedListener(new TextWatcher(){
            private String current = "";
            private String ddmmyyyy = "ddmmyyyy";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]", "");
                    String cleanC = current.replaceAll("[^\\d.]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }else{
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        if(mon > 12) mon = 12;
                        cal.set(Calendar.MONTH, mon-1);

                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    editNgaySinh.setText(current);
                    editNgaySinh.setSelection(sel < current.length() ? sel : current.length());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mssv = editMssv.getText().toString().trim();
                String hoTen = editHoTen.getText().toString().trim();
                String ngaySinh = editNgaySinh.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String queQuan = editQueQuan.getText().toString().trim();
                if (hoTen.equals("") || ngaySinh.equals("") || email.equals("") || queQuan.equals("")) {
                    dialogMessage("Thông tin còn thiếu!");
                }
                else {
                    database.QueryData("UPDATE Student SET HoTen='"+hoTen+"',NgaySinh='"+ngaySinh+"',Email='"+email+"',QueQuan='"+queQuan+"' WHERE Mssv="+Integer.parseInt(mssv)+" ");
                    dialogMessage("Sửa thông tin sinh viên thành công.");
                    dialog.cancel();
                    getDataFromSQL();
                }
            }
        });

        dialog.show();
    }

    // dialog xoa sinh vien
    public void dialogDeleteSV(final Student student) {
        final AlertDialog.Builder  alBuilder = new AlertDialog.Builder(this);
        alBuilder.setTitle("Thông báo!");
        alBuilder.setMessage("Bạn muốn xoá sinh viên có tên: "+student.getHoTen()+" ?");
        alBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int mssv = student.getMssv();
                database.QueryData("DELETE FROM Student WHERE Mssv="+mssv+" ");
                dialogMessage("Xoá sinh viên thành công");
                getDataFromSQL();
            }
        });
        alBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alBuilder.show();
    }

    //hien thi dialog thong bao 1 message nao do
    private void dialogMessage(String message){
        final AlertDialog.Builder  alBuilder = new AlertDialog.Builder(this);
        alBuilder.setTitle("Thông báo !");
        alBuilder.setMessage(message);
        alBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alBuilder.show();
    }

    //lay du lieu tu database
    private void getDataFromSQL() {
        studentList.clear();
        //get data
        Cursor dataSV = database.GetData("SELECT * FROM Student");
        while (dataSV.moveToNext()) {
            int mssv = dataSV.getInt(0);
            String hoTen = dataSV.getString(1);
            String ngaySinh = dataSV.getString(2);
            String email = dataSV.getString(3);
            String queQuan = dataSV.getString(4);
            //them vao data
            studentList.add(new Student(mssv,hoTen,ngaySinh,email,queQuan));
        }
        showData();
    }

    //hien thi ra recycle view
    private void showData(){
        //lay view hien thi
        final RecyclerView recyclerView = findViewById(R.id.list_view);
        recyclerView.setHasFixedSize(true);

        //thiet lap cach hien thi
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager); //noi voi view

        //ket noi toi adapter
        adapter = new StudentAdapter(studentList,this);
        recyclerView.setAdapter(adapter);
    }

    //tim kiem theo ma so sinh vien hoac ho ten trong database theo keyword
    private void getDataFromSQLBySearch(String keyword) {
        studentList.clear();

        //kiem tra chuoi tim kiem co chu cai hay ko
        int check = 0;
        for(int i = 0 ; i < keyword.length() ; i++) {
            if( !Character.isDigit(keyword.charAt(i)) ) {
                check = 1;
            }
        }
        //get data
        Cursor dataSV;
        if (check == 0) { //chuoi tim kiem toan chu so -> co the la tim theo mssv hoac ho ten
            dataSV = database.GetData("SELECT * FROM Student WHERE Mssv="+keyword+" OR HoTen like '%"+keyword+"%'");
        }
        else { //chuoi tim kiem co chu cai -> chac chan tim theo ho ten
            dataSV = database.GetData("SELECT * FROM Student WHERE HoTen like '%"+keyword+"%'");
        }
        while (dataSV.moveToNext()) {
            int mssv = dataSV.getInt(0);
            String hoTen = dataSV.getString(1);
            String ngaySinh = dataSV.getString(2);
            String email = dataSV.getString(3);
            String queQuan = dataSV.getString(4);
            //them vao data
            studentList.add(new Student(mssv,hoTen,ngaySinh,email,queQuan));
        }
        showData();
    }

}