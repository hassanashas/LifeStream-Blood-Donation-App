package com.example.lifestream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BloodGroupSelected extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<MyModel> arrList;
    String blood_group;
    MyBloodSelectedAdapter myAdapter;
    String lat, lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_group_selected);

        toolbar = findViewById(R.id.main_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.bloodgroup_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(BloodGroupSelected.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        arrList = new ArrayList<>();
        blood_group = getIntent().getStringExtra("bloodgroup");
        if (!blood_group.equals("compatible"))
        {
            getSupportActionBar().setTitle("Blood Group " + blood_group);
            getUsers(false);
        }
        else
        {
            getSupportActionBar().setTitle("People Compatible With You");
            getUsers(true);
        }
        lat = getIntent().getStringExtra("lat");
        lng = getIntent().getStringExtra("lng");


    }



    private void getUsers(boolean fh) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String usertype;
                if (snapshot.child("usertype").getValue().toString().equals("Donor"))
                    usertype = "Receipant";
                else
                    usertype = "Donor";
                String bloodgroup;
                if (fh)
                    bloodgroup = snapshot.child("bloodgroup").getValue().toString();
                else
                    bloodgroup = blood_group;

                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("users");
                Query query = myRef.orderByChild("name");
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrList.clear();
                        for (DataSnapshot data: snapshot.getChildren())
                        {
                            MyModel model = data.getValue(MyModel.class);
                            if (model.getBloodgroup().equals(bloodgroup))
                            {
                                if (model.getUsertype().equals(usertype))
                                    arrList.add(model);
                            }
                            myAdapter = new MyBloodSelectedAdapter(getApplicationContext(), arrList);
                            recyclerView.setAdapter(myAdapter);
                            myAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}