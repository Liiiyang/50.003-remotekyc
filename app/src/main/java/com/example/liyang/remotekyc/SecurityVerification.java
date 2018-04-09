package com.example.liyang.remotekyc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by Li Yang on 5/4/2018.
 */

public class SecurityVerification extends AppCompatActivity {

    private Button authenticate;
    private EditText edit;
    private FirebaseDatabase mref;
    private DatabaseReference mDatabase;
    private Query addData;
    private Signature sig;
    private KeyFactory kf;
    private byte[] signature;
    private ArrayList<String> listofPublicKeys = new ArrayList<String>();
    private ArrayList<String> listofNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_verification);
        authenticate = (Button) findViewById(R.id.authenticate);
        final TextView registerlink = (TextView) findViewById(R.id.register);
        authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit = (EditText) findViewById(R.id.editpk);
                final String getpk = edit.getText().toString().trim();
                final String[] name = new String[1];
                final String[] chosen_name = new String[1];
                //Test Private Key
                //String getpk = "MHsCAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQEEYTBfAgEBBBj/Q7kDI8G2yCiQPKUyoRa/zOzBELqpSCSgCgYIKoZIzj0DAQGhNAMyAARe0PM9xUSrEE6LQ4EhstMTH15b0ZYcB4NSKdA4XDFTqL5gNxjf" +
                //"+iK9vPCgT9TfUoc=";
                mref = FirebaseDatabase.getInstance();
                mDatabase = mref.getReference();
                byte[] privateKeyBytes = Base64.decode(getpk, Base64.DEFAULT);
                final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                // create a challenge
                final byte[] challenge = new byte[10000];
                ThreadLocalRandom.current().nextBytes(challenge);
                // sign using the private key
                try {
                    Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
                    kf = KeyFactory.getInstance("ECDSA", "SC");
                    PrivateKey privKey = kf.generatePrivate(keySpec);
                    sig = Signature.getInstance("ECDSA", "SC");
                    sig.initSign(privKey);
                    sig.update(challenge);
                    signature = sig.sign();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                //Toast.makeText(SecurityVerification.this,"Private Key: " + getpk, Toast.LENGTH_LONG).show();
                final boolean[] verified = new boolean[1];
                final boolean[] checked = new boolean[1];
                addData =
                        FirebaseDatabase.getInstance().getReference("Users").orderByChild("Publickey");
                ValueEventListener valueEventListener = addData.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                String publickey = data.child("Publickey").getValue(String.class);
                                listofPublicKeys.add(publickey);
                                name[0] = data.child("name").getValue(String.class);
                                listofNames.add(name[0]);
                                //Toast.makeText(SecurityVerification.this,publickey, Toast.LENGTH_LONG).show();
                                System.out.println("Added");

                            }
                            //Toast.makeText(SecurityVerification.this, listofPublicKeys.get(0), Toast.LENGTH_LONG).show();
                        } else {
                            System.out.println("Not Added");
                        }
                        int i = 0;
                        while(i<listofPublicKeys.size()){
                            String test = listofPublicKeys.get(i);
                            byte[] publicKeyBytes = Base64.decode(test, Base64.DEFAULT);
                            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
                            try {
                                PublicKey pubKey = kf.generatePublic(spec);
                                sig.initVerify(pubKey);
                                sig.update(challenge);
                                verified[0] = sig.verify(signature);
                                //Toast.makeText(SecurityVerification.this, Boolean.toString(verified[0]), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (verified[0]) {
                                checked[0] = true;
                                chosen_name[0] = listofNames.get(i);
                                break;
                            } else {
                                i++;
                                checked[0] = false;
                            }
                        }




                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
                Toast.makeText(SecurityVerification.this,"Verifying..Please Wait..", Toast.LENGTH_LONG).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if(checked[0]){
                            Toast.makeText(SecurityVerification.this,R.string.crypto_success, Toast.LENGTH_LONG).show();
                            Intent auth = new Intent(SecurityVerification.this, SomeHomePage.class);
                            auth.putExtra("name",chosen_name[0]);
                            startActivity(auth);
                            //Toast.makeText(SecurityVerification.this,name[0], Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(SecurityVerification.this,"Verification Failed: Contact Administrator Immediately", Toast.LENGTH_LONG).show();
                        }
                    }

                },2000);

            }
        });


    }
}

