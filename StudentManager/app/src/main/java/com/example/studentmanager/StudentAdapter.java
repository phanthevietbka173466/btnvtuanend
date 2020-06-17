package com.example.studentmanager;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Student> studentList;
    MainActivity context;

    public StudentAdapter(List<Student> studentList, MainActivity context) {
        this.studentList = studentList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_view,parent,false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StudentViewHolder viewHolder = (StudentViewHolder) holder;
        final Student student = studentList.get(position);

        viewHolder.textMSSV.setText(student.getMssv()+"");
        viewHolder.textHoTen.setText(student.getHoTen());
        viewHolder.textNgaySinh.setText(student.getNgaySinh());
        viewHolder.textEmail.setText(student.getEmail());
        viewHolder.textQueQuan.setText(student.getQueQuan());

        //bat su kien xoa va sua
        viewHolder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.dialogEditSV(student);
            }
        });
        viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.dialogDeleteSV(student);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    //tao doi tuong viewholder
    class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView textMSSV;
        TextView textHoTen;
        TextView textNgaySinh;
        TextView textEmail;
        TextView textQueQuan;
        ImageButton btn_edit;
        ImageButton btn_delete;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            textMSSV = itemView.findViewById(R.id.text_mssv);
            textHoTen = itemView.findViewById(R.id.text_hoTen);
            textNgaySinh = itemView.findViewById(R.id.text_ngaySinh);
            textEmail = itemView.findViewById(R.id.text_email);
            textQueQuan = itemView.findViewById(R.id.text_queQuan);
            btn_edit = itemView.findViewById(R.id.btn_edit);
            btn_delete = itemView.findViewById(R.id.btn_delete);
        }
    }

}
