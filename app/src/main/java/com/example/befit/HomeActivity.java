package com.example.befit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // splash UI

        new Handler().postDelayed(() -> {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                // ✅ User already logged in
                startActivity(new Intent(HomeActivity.this, DashboardActivity.class));
            } else {
                // ❌ New user or logged out
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            }

            finish();




        }, 3000); // 3 seconds splash
    }
}
