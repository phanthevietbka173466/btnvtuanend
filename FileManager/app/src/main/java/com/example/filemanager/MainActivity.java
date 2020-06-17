package com.example.filemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> items, copyDir;
    ListView listView;
    String SdPath, rootDir, dialogCopyPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        listView = findViewById(R.id.list_item);
        items = new ArrayList<>();

        // lay duong dan goc cua sdcard
        rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();

        // Hoi quyen truy nhap tu user
        askPermissionFromUser();

        //hien thi Root Directory lan dau
        SdPath = rootDir;
        getInfDir(SdPath);

        // ham callback khi item trong listview duoc an
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String f = items.get(position);
                File tmp = new File(SdPath + "/" + f);
                if ( tmp.exists() && !tmp.isFile() ) {  //neu la an vao folder
                    //noi them vao duong dan
                    SdPath = SdPath + "/" + items.get(position);
                    getInfDir(SdPath);
                }
                else {  //neu la an vao file
                    //open file
                    try {
                        String path = SdPath + "/" + f;
                        File file2open = new File(path);
                        String fileExtension = file2open.getAbsolutePath().substring(path.lastIndexOf("."));
                        String type = "text/plain";
                        Intent fileIntent =new Intent(Intent.ACTION_VIEW);
                        fileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Uri apkURI = FileProvider.getUriForFile(getApplicationContext(),getApplicationContext().getPackageName() + ".provider", file2open);
                        if(fileExtension.equalsIgnoreCase(".doc")||fileExtension.equalsIgnoreCase(".docx"))
                            type = "application/msword";
                        else if(fileExtension.equalsIgnoreCase(".pdf"))
                            type = "application/pdf";
                        else if(fileExtension.equalsIgnoreCase(".xls")||fileExtension.equalsIgnoreCase(".xlsx"))
                            type = "application/vnd.ms-excel";
                        else if(fileExtension.equalsIgnoreCase(".wav")||fileExtension.equalsIgnoreCase(".mp3"))
                            type = "audio/x-wav";
                        else if(fileExtension.equalsIgnoreCase(".jpg")||fileExtension.equalsIgnoreCase(".jpeg")||fileExtension.equalsIgnoreCase(".png"))
                            type = "image/jpeg";
                        else if(fileExtension.equalsIgnoreCase(".txt"))
                            type = "text/plain";
                        else if(fileExtension.equalsIgnoreCase(".3gp")||fileExtension.equalsIgnoreCase(".mp4")||fileExtension.equalsIgnoreCase(".avi"))
                            type = "video/*";
                        else
                            type = "*/*";
                        fileIntent.setDataAndType(apkURI, type);
                        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        getApplicationContext().startActivity(fileIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "Can't Find Your File", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    //tao option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //tao su kien an nut cho item trong option menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back_directory) {
            //neu duong dan hien tai khac duong dan goc thi moi cho back
            if ( !SdPath.equals(rootDir) ) {
                while (SdPath.charAt(SdPath.length()-1) != '/') {
                    SdPath = SdPath.substring(0, SdPath.length() - 1);
                }
                //cat tiep dau /
                SdPath = SdPath.substring(0, SdPath.length() - 1);
                //load lai thu muc
                getInfDir(SdPath);
            }
        }
        else if (id == R.id.action_add_file) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_add_file_txt);
            final EditText editNameFolder = dialog.findViewById(R.id.edit_name_file);
            final EditText editContentFile = dialog.findViewById(R.id.edit_content_file);
            Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
            Button btn_create = dialog.findViewById(R.id.btn_create);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            btn_create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Code to create a file va ghi du lieu vao
                    String nameFile = editNameFolder.getText().toString().trim();
                    String contentFile = editContentFile.getText().toString();
                    String path = SdPath + "/" + nameFile + ".txt";
                    File newFile = new File(path);
                    try {
                        FileWriter writer =new FileWriter(newFile,true);
                        writer.append(contentFile);
                        writer.flush();
                        writer.close();
                        Toast.makeText(MainActivity.this,"Add file " + nameFile + " successfully",Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        getInfDir(SdPath); //load lai cay thu muc
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_LONG).show();
                    }
                }
            });
            dialog.show();
        }
        else if (id == R.id.action_add_folder) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_add_folder);
            final EditText editNameFolder = dialog.findViewById(R.id.edit_name_folder);
            Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
            Button btn_create = dialog.findViewById(R.id.btn_create);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            btn_create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Code to create a folder
                    String nameFolder = editNameFolder.getText().toString().trim();
                    String path = SdPath + "/" + nameFolder;
                    File newFolder = new File(path);
                    if ( !newFolder.exists() ) {
                        newFolder.mkdir();
                        Toast.makeText(MainActivity.this,"Create " + nameFolder + " successfully",Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        getInfDir(SdPath); //load lai cau truc thu muc hien tai
                    }
                    else {
                        Toast.makeText(MainActivity.this, nameFolder + " existed", Toast.LENGTH_LONG).show();
                    }
                }
            });
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    //tao context menu -> chuc nang sua xoa thu muc
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Choose action ?");
        menu.add(0, 0, 0, "Rename");
        menu.add(0, 1, 0, "Delete");
        menu.add(0, 2, 0, "Copy to");
    }

    //su kien an nut trong context menu
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        //lay duoc vi tri item duoc an trong adapter -> lay duoc gia tri duoc an
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final String oldName = items.get(info.position);
        int id = item.getItemId();
        if (id == 0) {      //neu la edit name
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_edit_name);
            final EditText editNameFolder = dialog.findViewById(R.id.edit_name_folder);
            editNameFolder.setText(oldName);
            Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
            Button btn_update = dialog.findViewById(R.id.btn_update);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            btn_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newName = editNameFolder.getText().toString().trim();
                    if (newName != null) {
                        // Code to edit name
                        File old = new File(SdPath + "/"+ oldName);
                        File new1 = new File(SdPath + "/"+ newName);
                        boolean check = old.renameTo(new1);
                        if (check) {
                            Toast.makeText(MainActivity.this, "Rename " + oldName + " successfully", Toast.LENGTH_LONG).show();
                            getInfDir(SdPath); //load lai cay thu muc
                            dialog.cancel();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Input can't null", Toast.LENGTH_LONG).show();
                    }
                }
            });
            dialog.show();
        }
        else if (id == 1) {    //neu la xoa folder hoac file
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            AlertDialog dialog = builder.setMessage("Are you sure you want to delete " + oldName + " ?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Code to delete folder or file
                            File fileDelete = new File(SdPath + "/" + oldName);
                            deleteFile(fileDelete);
                            getInfDir(SdPath); //load lai cay thu muc
                            Toast.makeText(MainActivity.this,"Delete successfully",Toast.LENGTH_LONG).show();
                        }
                    })
                    .create();
            dialog.show();
        }
        else if (id == 2) {     //copy to (chi copy duoc file)
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_copy_file);
            final ListView list = dialog.findViewById(R.id.list_folder_copy);
            Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
            Button btn_copy = dialog.findViewById(R.id.btn_copy);
            ImageButton btn_back = dialog.findViewById(R.id.btn_back);

            // view cua dialog
            dialogCopyPath = rootDir;
            copyDir = new ArrayList<>();
            getInfDirCopy(list,dialogCopyPath);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String f = copyDir.get(position);
                    File tmp = new File(dialogCopyPath + "/" + f);
                    if ( tmp.exists() && !tmp.isFile() ) {  //neu la an vao folder
                        //noi them vao duong dan
                        dialogCopyPath = dialogCopyPath + "/" + f;
                        getInfDirCopy(list,dialogCopyPath);
                    }
                }
            });

            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //neu duong dan hien tai khac duong dan goc thi moi cho back
                    if ( !dialogCopyPath.equals(rootDir) ) {
                        while (dialogCopyPath.charAt(dialogCopyPath.length()-1) != '/') {
                            dialogCopyPath = dialogCopyPath.substring(0, dialogCopyPath.length() - 1);
                        }
                        //cat tiep dau /
                        dialogCopyPath = dialogCopyPath.substring(0, dialogCopyPath.length() - 1);
                        //load lai thu muc
                        getInfDirCopy(list,dialogCopyPath);
                    }
                }
            });

            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });

            btn_copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Code to copy
                    String newName = "copy_" + oldName;
                    String destinationPath = dialogCopyPath + "/" + newName ;
                    String sourcePath = SdPath + "/" + oldName;
                    try {
                        InputStream is = new FileInputStream(new File(sourcePath));
                        OutputStream os = new FileOutputStream(new File(destinationPath));
                        OutputStreamWriter writer = new OutputStreamWriter(os);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) != -1)
                            os.write(buffer, 0, len);
                        writer.close();
                        os.close();
                        Toast.makeText(MainActivity.this, "Copy file successfully", Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                    dialog.cancel();
                }
            });
            dialog.show();
        }

        return super.onContextItemSelected(item);
    }

    //ham su dung de quy de xoa file hoac folder
    private void deleteFile(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteFile(child);
            }
        }
        file.delete();
    }

    // ham lay noi dung tu duong dan
    private void getInfDir(String path) {
        // lam moi list view
        items.clear();
        //lay lai noi dung
        File file = new File(path);
        String[] list = file.list();
        try {
            for (int i = 0 ; i < list.length ; i++) {
                items.add(list[i]);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        // hien thi ra view
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
        listView.setAdapter(adapter);
        //dang ki su kien an item de hien thi context menu
        listView.setLongClickable(true);
        registerForContextMenu(listView);
    }

    // ham lay noi dung tu duong dan hien thi trong dialog copy
    private void getInfDirCopy(ListView listView, String path) {
        // lam moi list view
        copyDir.clear();
        //lay lai noi dung
        File file = new File(path);
        String[] list = file.list();
        try {
            for (int i = 0 ; i < list.length ; i++) {
                copyDir.add(list[i]);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        // hien thi ra view
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,copyDir);
        listView.setAdapter(adapter);
    }

    // ham hoi y kien truy cap sdcard
    private void askPermissionFromUser() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Permission Denied. Asking for permission.",Toast.LENGTH_LONG);
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1234);
            }
        }
    }

    // ham callback cua ham hoi y kien su dung quyen sdcard
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1234) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Denied.",Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this,"Permission Granted.",Toast.LENGTH_LONG).show();
        }
    }

}