package com.example.lifestream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button log_out_button;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    RecyclerView recyclerView;
    CircleImageView profile_image;
    TextView nav_name, nav_type, nav_email, nav_bloodgroup;
    String lat, lng;


    ArrayList<MyModel> arrList;
    MyAdapter myAdapter;
    ProgressBar progressBar;

    DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        progressBar = findViewById(R.id.progressbar);

        drawerLayout = findViewById(R.id.MainDrawerLayout);
        toolbar = findViewById(R.id.main_tool_bar);
        navigationView = findViewById(R.id.nav_bar);

        navigationView.setNavigationItemSelectedListener(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("LifeStream");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(UserMainActivity.this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Linking the Menu Items
        profile_image = navigationView.getHeaderView(0).findViewById(R.id.nav_profile_pic);
        nav_name = navigationView.getHeaderView(0).findViewById(R.id.nav_user_name);
        nav_type = navigationView.getHeaderView(0).findViewById(R.id.nav_user_type);
        nav_email = navigationView.getHeaderView(0).findViewById(R.id.nav_user_email);
        nav_bloodgroup = navigationView.getHeaderView(0).findViewById(R.id.nav_user_bloodgroup);

        myRef = FirebaseDatabase.getInstance().getReference().child("users").child(
                FirebaseAuth.getInstance().getCurrentUser().getUid());

        // To get The data of logged in User
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    nav_name.setText(snapshot.child("name").getValue().toString());
                    nav_type.setText(snapshot.child("usertype").getValue().toString());
                    nav_email.setText(snapshot.child("email").getValue().toString());
                    nav_bloodgroup.setText(snapshot.child("bloodgroup").getValue().toString());
                    lat = snapshot.child("latitude").getValue().toString();
                    lng = snapshot.child("longitude").getValue().toString();

                    if (snapshot.hasChild("profilepic"))
                    {
                        String imgurl = snapshot.child("profilepic").getValue().toString();
                        Glide.with(getApplicationContext()).load(imgurl).into(profile_image);
                    }
                    else
                    {
                        profile_image.setImageResource(R.drawable.registration_profile_image);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        recyclerView = findViewById(R.id.users_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UserMainActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        arrList = new ArrayList<>();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type = snapshot.child("usertype").getValue().toString();
                if (type.equals("Donor"))
                {
                    getRecipients();
                }
                else
                {
                    getDonors();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // Get the Donors
    private void getDonors() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = reference.orderByChild("usertype").equalTo("Donor");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrList.clear();
                for (DataSnapshot data : snapshot.getChildren())
                {
                    MyModel model = data.getValue(MyModel.class);
                    model.setLng(data.child("longitude").getValue().toString());
                    model.setLat(data.child("latitude").getValue().toString());
                    Double d = Util.distance(Double.parseDouble(lat), Double.parseDouble(lng),
                            Double.parseDouble(model.getLat()), Double.parseDouble(model.getLng()));
                    DecimalFormat df = new DecimalFormat("#.##");
                    double distance = Double.parseDouble(df.format(d));
                    model.setDistance(distance);
                    arrList.add(model);
                }

                Collections.sort(arrList, new Comparator<MyModel>() {
                    @Override
                    public int compare(MyModel z1, MyModel z2) {
                        if (z1.distance < z2.distance)
                            return 1;
                        if (z1.distance > z2.distance)
                            return -1;
                        return 0;
                    }
                });

                myAdapter = new MyAdapter(getApplicationContext(), arrList, lat, lng);
                recyclerView.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (arrList.isEmpty())
                {
                    Toast.makeText(UserMainActivity.this, "No Donors Registered", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Get the Recipients
    private void getRecipients() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = reference.orderByChild("usertype").equalTo("Receipant");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrList.clear();
                for (DataSnapshot data : snapshot.getChildren())
                {
                    MyModel model = data.getValue(MyModel.class);
                    model.setLng(data.child("longitude").getValue().toString());
                    model.setLat(data.child("latitude").getValue().toString());
                    Double d = Util.distance(Double.parseDouble(lat), Double.parseDouble(lng),
                            Double.parseDouble(model.getLat()), Double.parseDouble(model.getLng()));
                    DecimalFormat df = new DecimalFormat("#.##");
                    double distance = Double.parseDouble(df.format(d));
                    model.setDistance(distance);
                    arrList.add(model);
                }

                Collections.sort(arrList, new Comparator<MyModel>() {
                    @Override
                    public int compare(MyModel z1, MyModel z2) {
                        if (z1.distance < z2.distance)
                            return 1;
                        if (z1.distance > z2.distance)
                            return -1;
                        return 0;
                    }
                });

                myAdapter = new MyAdapter(getApplicationContext(), arrList, lat, lng);
                recyclerView.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (arrList.isEmpty())
                {
                    Toast.makeText(UserMainActivity.this, "No Recipients Registered", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    // For the Menu Item Clicks
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;

        switch(item.getItemId())
        {
            case R.id.menu_profile:
                 intent = new Intent(UserMainActivity.this, UpdateProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_OP:
                intent = new Intent(UserMainActivity.this, BloodGroupSelected.class);
                intent.putExtra("bloodgroup", "O+");
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lng);
                startActivity(intent);
                break;
            case R.id.menu_ON:
                intent = new Intent(UserMainActivity.this, BloodGroupSelected.class);
                intent.putExtra("bloodgroup", "O-");
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lng);
                startActivity(intent);
                break;
            case R.id.menu_AP:
                intent = new Intent(UserMainActivity.this, BloodGroupSelected.class);
                intent.putExtra("bloodgroup", "A+");
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lng);
                startActivity(intent);
                break;
            case R.id.menu_AN:
                intent = new Intent(UserMainActivity.this, BloodGroupSelected.class);
                intent.putExtra("bloodgroup", "A-");
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lng);
//                intent.putExtra("usertype", current_usertype);
                startActivity(intent);
                break;
            case R.id.menu_BP:
                intent = new Intent(UserMainActivity.this, BloodGroupSelected.class);
                intent.putExtra("bloodgroup", "B+");
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lng);
                startActivity(intent);
                break;
            case R.id.menu_BN:
                intent = new Intent(UserMainActivity.this, BloodGroupSelected.class);
                intent.putExtra("bloodgroup", "B-");
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lng);
//                intent.putExtra("usertype", current_usertype);
                startActivity(intent);
                break;
            case R.id.menu_ABP:
                intent = new Intent(UserMainActivity.this, BloodGroupSelected.class);
                intent.putExtra("bloodgroup", "AB+");
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lng);
//                intent.putExtra("usertype", current_usertype);
                startActivity(intent);
                break;
            case R.id.menu_ABN:
                intent = new Intent(UserMainActivity.this, BloodGroupSelected.class);
                intent.putExtra("bloodgroup", "AB-");
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lng);
//                intent.putExtra("usertype", current_usertype);
                startActivity(intent);
                break;
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(UserMainActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(UserMainActivity.this, "Logged out Successfully", Toast.LENGTH_SHORT).show();
                finish();
                break;

            case R.id.menu_compatible:
                intent = new Intent(UserMainActivity.this, BloodGroupSelected.class);
                intent.putExtra("bloodgroup", "compatible");
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lng);
                startActivity(intent);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}