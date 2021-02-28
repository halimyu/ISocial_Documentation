package com.app.isocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.app.isocial.Fragment.HomeFragment;
import com.app.isocial.Fragment.ProfileFragment;
import com.app.isocial.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AfterLoginRegister extends AppCompatActivity {

    BottomNavigationView btmnv;
    Fragment sFragment;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    // Check if user is signed in (non-null) on the start of the app and update UI accordingly
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login_register);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(AfterLoginRegister.this, MainActivity.class));
                    AfterLoginRegister.this.finish();
                }
            }
        };

        btmnv = findViewById(R.id.bottom_navigation);

        btmnv.setOnNavigationItemSelectedListener(navItemSelected);

        Bundle intent = getIntent().getExtras();

        if (intent != null) {

            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

        } else{
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navItemSelected =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()){

                        case R.id.nav_home:
                            sFragment = new HomeFragment();
                            break;

                        case R.id.nav_search:
                            sFragment = new SearchFragment();
                            break;

                        case R.id.nav_add:
                            sFragment = null;
                            startActivity(new Intent(AfterLoginRegister.this, PostActivity.class));
                            break;

                        case R.id.nav_profile:
                            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                            editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.apply();
                            sFragment = new ProfileFragment();
                            break;

                    }

                    if (sFragment != null){
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, sFragment).commit();
                    }

                    return true;
                }
            };

}