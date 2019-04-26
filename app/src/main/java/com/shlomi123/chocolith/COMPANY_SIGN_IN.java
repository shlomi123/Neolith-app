package com.shlomi123.chocolith;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class COMPANY_SIGN_IN extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private EditText email;
    private EditText password;
    private TextView reset;
    private Button button;
    private Button change_type;
    private ProgressBar spinner;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company__sign__in);

        //change entry point to company sign in
        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("USER_TYPE", 3);
        editor.apply();

        email = (EditText) findViewById(R.id.editText_signIn_email);
        password = (EditText) findViewById(R.id.editText_signIn_password);
        button = (Button) findViewById(R.id.button_signIn);
        change_type = (Button) findViewById(R.id.button_company_change_user_type);
        reset = (TextView) findViewById(R.id.textView_company_forgot_password);
        spinner = (ProgressBar)findViewById(R.id.progressBar_signIn);
        spinner.setVisibility(View.INVISIBLE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email.setVisibility(View.INVISIBLE);
                password.setVisibility(View.INVISIBLE);
                button.setVisibility(View.INVISIBLE);
                spinner.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful())
                                {
                                    signIn();
                                }else{
                                    email.setVisibility(View.VISIBLE);
                                    password.setVisibility(View.VISIBLE);
                                    button.setVisibility(View.VISIBLE);
                                    spinner.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        change_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putInt("USER_TYPE", 0);
                editor.apply();
                startActivity(new Intent(COMPANY_SIGN_IN.this, ENTRY_POINT.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText edittext = new EditText(getApplicationContext());
                final AlertDialog alert = new AlertDialog.Builder(COMPANY_SIGN_IN.this)
                        .setMessage("please enter your email")
                        .setTitle("Reset Password")
                        .setView(edittext)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String email = edittext.getText().toString();

                                mAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(COMPANY_SIGN_IN.this, "email sent to reset password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }).setNegativeButton("Cancel", null)
                        .create();
                alert.show();
            }
        });
    }

    private void signIn(){

        FirebaseUser user = mAuth.getCurrentUser();



        if (user.isEmailVerified()){
            db.collection("Companies").document(email.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        editor.putString("COMPANY_PROFILE", task.getResult().getString("Profile"));
                        editor.putInt("USER_TYPE", 1);
                        editor.apply();
                        startActivity(new Intent(COMPANY_SIGN_IN.this, ADMIN_MAIN_PAGE.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }
                }
            });

        }else{
            email.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "email wasn't verified", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
        }

    }
}
