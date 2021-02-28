package com.app.isocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
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

public class Login extends AppCompatActivity {

    //Calling materials from the UI file and assigning to variables
    SignInButton sign;
    MaterialButton reg, login;
    TextInputLayout email, password;
    TextView frgtPass;
    RelativeLayout progresBar;

    LoginButton loginButton;
    CallbackManager mCallbackManager;

    // sitting variables for google clients and firebase authentication
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 2;
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference databaseReference;

    // Check if user is signed in (non-null) on the start of the app and update UI accordingly
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initializing variables to textLayout, textViews, and buttons;
        email = findViewById(R.id.loginLayoutEmail);
        password = findViewById(R.id.loginLayoutPass);
        sign = findViewById(R.id.sign_in_button);
        login = findViewById(R.id.login);
        reg = findViewById(R.id.backRegister);
        frgtPass = findViewById(R.id.frgtPass);
        loginButton = findViewById(R.id.login_button);

        progresBar = findViewById(R.id.prgbar);

        // getting firebase auth
        mAuth = FirebaseAuth.getInstance();



        // if user is nonnull end this activity and go to afterLogin
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(Login.this, AfterLoginRegister.class));
                    Login.this.finish();
                }
            }
        };

        // initializing view for snackbar messages
        final View snack = findViewById(R.id.loginLayout);

        // if reg is clicked switch to register page.
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        // sitting a click listener for forgot password
        frgtPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // checking if validation returns false when button is clicked
                if (!validateEmail()){
                    Snackbar.make(snack, "Please enter your email to reset your password", Snackbar.LENGTH_LONG).show();
                    return;
                }
                progresBar.setVisibility(View.VISIBLE);
                // if true, Authenticate login
                String lEmail = email.getEditText().getText().toString().trim();
                mAuth.sendPasswordResetEmail(lEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "Email Sent");
                                    Snackbar.make(snack, "Email Sent", Snackbar.LENGTH_LONG).show();
                                    email.getEditText().setText("");
                                    password.getEditText().setText("");
                                    progresBar.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "signInWithEmail:failure", task.getException());
                                    Snackbar.make(snack, "" + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                    email.getEditText().setText("");
                                    password.getEditText().setText("");
                                    progresBar.setVisibility(View.INVISIBLE);
                                }

                            }
                        });


            }
        });

        // sitting a click listener for login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // checking if validation returns false when button is clicked
                if (!validateEmail() | !validatePassword()){
                    Snackbar.make(snack, "Please correct the errors in the fields above!", Snackbar.LENGTH_LONG).show();
                    return;
                }
                progresBar.setVisibility(View.VISIBLE);
                // if true, Authenticate login
                String lEmail = email.getEditText().getText().toString().trim();
                String lPass = password.getEditText().getText().toString().trim();
                // Validating user login
                    mAuth.signInWithEmailAndPassword(lEmail, lPass)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("TAG", "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        progresBar.setVisibility(View.INVISIBLE);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("TAG", "signInWithEmail:failure", task.getException());
                                        Snackbar.make(snack, "" + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                        progresBar.setVisibility(View.INVISIBLE);
                                        password.getEditText().setText("");
                                    }
                                }
                            });

            }
        });

        // when google sign in button is clicked
        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Configuring Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient;
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
                Toast.makeText(Login.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
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

                            FirebaseDatabase.getInstance().getReference().child("Follow").child(id)
                                    .child("following").child(id).setValue(true);
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(id)
                                    .child("followers").child(id).setValue(true);

                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

                            HashMap<String, Object> hash = new HashMap<>();
                            hash.put("id", id);
                            hash.put("name", account.getDisplayName());
                            hash.put("bio", "");
                            hash.put("username", account.getDisplayName().toLowerCase());
                            hash.put("imageurl", account.getPhotoUrl().toString());

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
                                        Toast.makeText(Login.this, "" + task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        progresBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progresBar.setVisibility(View.INVISIBLE);
                        }


                    }
                });
    }

    // checking the email is in proper format
    private boolean validateEmail() {
        String emailInput = email.getEditText().getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Please make sure your email is in the form of example@example.com");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }
    // checking if password is not empty, no need to check format since its login
    private boolean validatePassword() {
        String passwordInput = password.getEditText().getText().toString().trim();
        if (passwordInput.isEmpty()) {
            password.setError("Password can't be empty");
            password.getEditText().setText("");
            return false;
        } else {
            password.setError(null);
            return true;
        }
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
                                                    Toast.makeText(Login.this, "" + task.getException().getMessage(),
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
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

}