package com.app.isocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    //Calling materials from the UI file and assigning to variables
    SignInButton sign;
    MaterialButton reg, login;
    TextInputLayout email, first, last, password, passVerfication;

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

    // Pattern for Password validation
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@!*#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 4 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // initializing variables to textLayout, textViews, and buttons;
        email = findViewById(R.id.regLayoutEmail);
        first = findViewById(R.id.regLayoutFirst);
        last = findViewById(R.id.regLayoutLast);
        password = findViewById(R.id.regLayoutPass);
        passVerfication = findViewById(R.id.regLayoutPassver);
        sign = findViewById(R.id.sign_in_button);
        login = findViewById(R.id.backLogin);
        reg = findViewById(R.id.register);
        loginButton = findViewById(R.id.login_button);

        progresBar = findViewById(R.id.prgbar);

        // getting firebase auth
        mAuth = FirebaseAuth.getInstance();

        // if user is nonnull end this activity and go to afterLogin
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(Register.this, AfterLoginRegister.class));
                    Register.this.finish();
                }
            }
        };

        // initializing view for snackbar messages
        final View snack = findViewById(R.id.regLayout);

        // if reg is clicked switch to register page.
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        // sitting a click listener for login button
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // checking if validation returns false when button is clicked
                if (!validateEmail() | !validatePassword() | !validateFirst() | !validateLast() | !reenterPassword()){
                    Snackbar.make(snack, "Please correct the errors in the fields above!", Snackbar.LENGTH_LONG).show();
                    return;
                }
                progresBar.setVisibility(View.VISIBLE);
                // if true, Authenticate login
                String rEmail = email.getEditText().getText().toString().trim();
                String rPass = password.getEditText().getText().toString().trim();

                // Validating user login
                mAuth.createUserWithEmailAndPassword(rEmail, rPass)
                        .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    //update database with user information
                                    Log.d("TAG", "createUserWithEmail:success");
                                    String firstInput = first.getEditText().getText().toString().trim();
                                    String lastInput = last.getEditText().getText().toString().trim();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String id = user.getUid();

                                    mAuth.getCurrentUser().sendEmailVerification();

                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(id)
                                            .child("following").child(id).setValue(true);
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(id)
                                            .child("followers").child(id).setValue(true);

                                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

                                    HashMap<String, Object> hash = new HashMap<>();
                                    hash.put("id", id);
                                    hash.put("name", firstInput + " " + lastInput);
                                    hash.put("bio", "");
                                    hash.put("username", firstInput.toLowerCase() + " " + lastInput.toLowerCase());
                                    hash.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/isocial-645af.appspot.com/o/pngtree-profile-line-black-icon-png-image_691051.jpg?alt=media&token=c77a46c8-4e2f-46fd-a37f-e0541a0c30a2");

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
                                                Toast.makeText(Register.this, "" + task.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                                progresBar.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });


                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(Register.this, "" + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    password.getEditText().setText("");
                                    passVerfication.getEditText().setText("");
                                    progresBar.setVisibility(View.INVISIBLE);
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
                Toast.makeText(Register.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
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
                                        Toast.makeText(Register.this, "" + task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        progresBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(Register.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progresBar.setVisibility(View.INVISIBLE);
                        }


                    }
                });
    }

    // checking the email is in proper format
    private boolean validateEmail() {
        String emailInput = email.getEditText().getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Please make sure your email is in the form of \"example@example.com\"");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }

    // checking the email is in proper format
    private boolean validateFirst() {
        String firstInput = first.getEditText().getText().toString().trim();
        if (firstInput.length() < 3) {
            first.setError("Please type your first name!");
            return false;
        }
        else {
            first.setError(null);
            return true;
        }
    }

    // checking the email is in proper format
    private boolean validateLast() {
        String lastInput = last.getEditText().getText().toString().trim();
        if (lastInput.length() < 3) {
            last.setError("Please type your last name!");
            return false;
        }
        else {
            last.setError(null);
            return true;
        }
    }

    // checking if password is in proper format
    private boolean validatePassword() {
        String passwordInput = password.getEditText().getText().toString().trim();
        if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            password.setError("Please use at least 6 characters \"one uppercase, one lowercase, a number,  and a special character\"");
            password.getEditText().setText("");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    // checking if password is same as password verification
    private boolean reenterPassword() {
        String passwordInput = password.getEditText().getText().toString().trim();
        String password2Input = passVerfication.getEditText().getText().toString().trim();
        if (password2Input.isEmpty() || !passwordInput.equals(password2Input)) {
            passVerfication.setError("Passwords doesn't match! Please make sure your passwords are the same");
            password.getEditText().setText("");
            passVerfication.getEditText().setText("");
            return false;
        } else {
            passVerfication.setError(null);
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
                                                    Toast.makeText(Register.this, "" + task.getException().getMessage(),
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
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

}