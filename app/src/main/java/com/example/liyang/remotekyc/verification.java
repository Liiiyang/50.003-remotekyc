package com.example.liyang.remotekyc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Li Yang on 3/2/2018.
 */

public class verification extends AppCompatActivity {
    Button signout;
    private FirebaseAuth mAuth;
    TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_verification);
        mAuth = FirebaseAuth.getInstance();
        name = (TextView)findViewById(R.id.name);
        signout = (Button)findViewById(R.id.signout);
        //Check if User is already logged in
        if(mAuth.getCurrentUser() == null){
            //User not logged in
            finish();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
        //Get the Name of the Current User
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            name.setText("Welcome, " + user.getDisplayName());
        }

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

    }
}
