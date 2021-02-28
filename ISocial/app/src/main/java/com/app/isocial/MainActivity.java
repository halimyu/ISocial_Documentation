package com.app.isocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //Calling materials from the UI file and assigning to variables
    SignInButton sign;
    MaterialButton reg, login;
    RelativeLayout progresBar;

    LoginButton loginButton;
    CallbackManager mCallbackManager;

    DatabaseReference databaseReference;

    // Assigning database and google client to variables
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 2;
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

        setContentView(R.layout.activity_main);



        sign = findViewById(R.id.sign_in_button);
        reg = findViewById(R.id.toRegister);
        login = findViewById(R.id.toLogin);
        loginButton = findViewById(R.id.login_button);

        progresBar = findViewById(R.id.prgbar);


        // getting firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // switching to registration when register button is clicked
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Register.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Login.class));
            }
        });

        mCallbackManager = CallbackManager.Factory.create();

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("TAG", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("TAG", "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG", "facebook:onError", error);

            }
        });

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(MainActivity.this, AfterLoginRegister.class));
                    MainActivity.this.finish();
                }
            }
        };

        // Configuring Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progresBar.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

                            String id = user.getUid();

                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

                            HashMap<String, Object> hash = new HashMap<>();
                            hash.put("id", id);
                            hash.put("name", account.getDisplayName());
                            hash.put("bio", "");
                            hash.put("username", account.getDisplayName().toLowerCase());
                            hash.put("imageurl", account.getPhotoUrl().toString());

                            FirebaseDatabase.getInstance().getReference().child("Follow").child(id)
                                    .child("following").child(id).setValue(true);
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(id)
                                    .child("followers").child(id).setValue(true);

                                databaseReference.setValue(hash).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            //update database with user information
                                            Log.d("TAG", "createUserWithEmail:success");
                                            progresBar.setVisibility(View.INVISIBLE);
                                        }
                                        else {
                                            // If sign in fails, display a message to the user.
                                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(MainActivity.this, "" + task.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            progresBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            progresBar.setVisibility(View.INVISIBLE);
                        }


                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("TAG", "handleFacebookAccessToken:" + token);

        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();

                            GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {

                                    try {
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                                        HashMap<String, Object> hash = new HashMap<>();
                                        hash.put("id", user.getUid());
                                        hash.put("name", object.getString("name"));
                                        hash.put("bio", "");
                                        hash.put("username", object.getString("name").toLowerCase());
                                        hash.put("imageurl", "https://graph.facebook.com/" + object.getString("id") + "/picture?type=normal");

                                        FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUid())
                                                .child("following").child(user.getUid()).setValue(true);
                                        FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUid())
                                                .child("followers").child(user.getUid()).setValue(true);

                                        databaseReference.setValue(hash).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    //update database with user information
                                                    Log.d("TAG", "createUserWithEmail:success");
                                                    progresBar.setVisibility(View.INVISIBLE);
                                                }
                                                else {
                                                    // If sign in fails, display a message to the user.
                                                    Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                                    Toast.makeText(MainActivity.this, "" + task.getException().getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                    progresBar.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                            Bundle bundle = new Bundle();
                            bundle.putString("fields", "name, id");
                            graphRequest.setParameters(bundle);
                            graphRequest.executeAsync();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

}