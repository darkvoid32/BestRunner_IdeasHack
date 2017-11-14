package com.example.cxo05.bestrunner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = this.getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        if (sharedPref.getString("ID", "000000" ).equals("000000")) {
            //Register
            setContentView(R.layout.activity_main);
            name = findViewById(R.id.nameField);
        }else{
            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(intent);
        }
    }

    public void Signup(View v) {
        if (name.getText() == null || name.getText().toString().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Name cannot be empty")
                    .setTitle("Field empty");

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.child("accounts").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.hasChild(name.getText().toString())) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        builder.setMessage("Please register using a different name")
                                .setTitle("Name has been used");

                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return;
                    } else {
                        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                        sharedPref.edit().putString("ID", name.getText().toString()).apply();
                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                        rootRef.child("accounts").child(name.getText().toString()).setValue("null");
                        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("error", "wtf");
                }
            });
        }
    }
}
