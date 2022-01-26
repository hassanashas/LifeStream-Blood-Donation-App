package com.example.lifestream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyBloodSelectedAdapter extends RecyclerView.Adapter<MyBloodSelectedAdapter.ViewHolder>{

    Context context;
    ArrayList<MyModel> arrList;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_selected_blood_view_layout, parent, false);
        return new MyBloodSelectedAdapter.ViewHolder(view);
    }

    public MyBloodSelectedAdapter(Context context, ArrayList<MyModel> arrList) {
        this.context = context;
        this.arrList = arrList;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyModel model;
        model = arrList.get(position);

        holder.name.setText(model.getName());
        holder.usertype.setText(model.getUsertype());
        holder.email.setText(model.getEmail());
        holder.bloodgroup.setText(model.getBloodgroup());
        holder.phone.setText(model.getPhone());
        Glide.with(context).load(model.getProfilepic()).into(holder.profilepic);

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(view.getRootView().getContext())
                        .setTitle("Send Email")
                        .setMessage("Send Message to " + model.getName() + "?")
                        .setCancelable(false)
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                myRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String senderName = snapshot.child("name").getValue().toString();
                                        String senderEmail = snapshot.child("email").getValue().toString();
                                        String senderBloodgroup = snapshot.child("bloodgroup").getValue().toString();
                                        String senderPhone = snapshot.child("phone").getValue().toString();

                                        String mEmail = model.getEmail();
                                        String mSubject = "Request for your Blood Donation";
                                        String mMessage = "Dear " + model.getName() + ", hope you are doing well. \n" +
                                                senderName + " is in urgent need of Blood Donation and your blood type matches " +
                                                "the person's blood type. It is requested from you to please contact the person. " +
                                                "Details are shared below, \n"
                                                + "Name: " + senderName + "\n"
                                                + "Phone Number: " + senderPhone + "\n"
                                                + "Email: " + senderEmail + "\n"
                                                + "Blood Group: " + senderBloodgroup + "\n"
                                                + "\nKindly reach out to the person in need. " + "\n\n"
                                                + "Regards, \n"
                                                + "LifeStream Blood Donation Organization";

                                        MailClass mailClass = new MailClass(context, mEmail, mSubject, mMessage);
                                        mailClass.execute();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profilepic;
        TextView usertype, name, email, phone, bloodgroup, distance;
        View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilepic = itemView.findViewById(R.id.user_view_profile_image);
            usertype = itemView.findViewById(R.id.user_view_usertype);
            name = itemView.findViewById(R.id.user_view_name);
            email = itemView.findViewById(R.id.user_view_email);
            phone = itemView.findViewById(R.id.user_view_phone);
            bloodgroup = itemView.findViewById(R.id.user_view_bloodgroup);
            view = itemView.findViewById(R.id.user_view);
            distance = itemView.findViewById(R.id.user_view_distance);

        }
    }
}
