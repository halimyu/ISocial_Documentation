//The App Package Name, Should Not Be Edited!
package com.app.isocial;

//Imported classes that should not be edited
//Imported classes are automatically added when witting the code
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

//The Activity Main Class
public class FacebookShare extends AppCompatActivity {

    // Strings that will later be defined from the post needed to be shared
    String description;
    String postimage;

    // Callback for calling the "Facebook SDK" and shareDialog "To open Facebook Sharing Dialog".
    //Those will later be defined
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    /**
     * The On Create Method Is Automatically generated when The Activity is created
     * THis method calls The XML File "UI" associated with This Class
     * This Method Stores all Variables, Widgets, and Data collected from the XML File
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_share);

        // Defining The Facebook callBack and Creating it.
        callbackManager = CallbackManager.Factory.create();
        //Setting an new Share Dialog associated with the activity
        shareDialog = new ShareDialog(FacebookShare.this);

        /*
        Getting The String Data From the Intent That where Initialized when Opining FacebookShare
        Activity from PostAdapter.
        Initializing The Strings that were created Earlier to the String Data received from Intent .
         */
        Intent intent = getIntent();
        description = intent.getStringExtra("description");
        postimage = intent.getStringExtra("postimage");

        /* Adding All Strings Initialized from the Intent To one String to be able to add them in The
            Shared Content Later in the code
         */
        String postQuote = "Post shared from ISocial." +
                "\n Post Description:" + description;


        // Creating a Facebook ShareLinkContent to Share the Post Text along with the Post Image
        //Setting The Link Content quote to postQuote String and the content Url to the postImage
        ShareLinkContent linkContent = new ShareLinkContent.Builder().setQuote(postQuote)
                .setContentUrl(Uri.parse(postimage)).build();

        /*Checking if Share Dialog can safely Open ShareLinkContent.Class,
         if this is true the ShareDialog will Open with Previously initialized ShareLink Content "linkContent"
         */
        if (ShareDialog.canShow(ShareLinkContent.class)){
            shareDialog.show(linkContent);
        }

        //Finishing the Activity when the all the previous codes are done
        FacebookShare.this.finish();

    }

}