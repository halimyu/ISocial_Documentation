//The App Package Name, Should Not Be Edited!
package com.app.isocial.Adapter;

//Imported classes that should not be edited
//Imported classes are automatically added when witting the code
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.isocial.AfterLoginRegister;
import com.app.isocial.Model.Comment;
import com.app.isocial.Model.User;
import com.app.isocial.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/*
 * The Adapter Main Class
 * The Class is extended to with RecyclerView so When the The Adapter Is called from From another Activity
   it can be easily added to a RecyclerView
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    //Initializing a Context to the the Adapter
    private Context mContext;
    // Initializing a List from the Comment.Class located in Model
    private List<Comment> mComment;
    // Intializing a String that will later be defined as the postid that is gtting the comment
    private String postid;
    // intializing a FirebaseUser to Later call the current user ID
    private FirebaseUser firebaseUser;

    //Constructor for the Adapter Context, postid, and PostList
    public CommentAdapter(Context mContext, List<Comment> mComment, String postid) {
        this.mContext = mContext;
        this.mComment = mComment;
        this.postid = postid;
    }


    //automatically Generated method for the Adapter
    //used to call and inflate the comment_item XML file in a RecyclerView when the CommentAdapter class is called
    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       // Initializing and defining view to inflate the Layout XML file
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);

        // returning the new view
        return new CommentAdapter.ViewHolder(view);
    }

    //automatically Generated method for the Adapter
    // This method contain all the functions that the widgets located on the XML file "comment_item" should do
    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {

        // defining firebaseUser to get the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Defining Comment class as the the List defined earlier with regards to position
        final Comment comment = mComment.get(position);

        //Calling the comment TextView Widget to set the Text as the Post Comment
        holder.comment.setText(comment.getComment());

        // Calling the getUserInfo Methos to show the User Info in the Comment List
        getUserInfo(holder.image_profile, holder.name, comment.getPublisher());

        //Setting a click Listener for the comment to open the user profile
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // creating an Intent to switch to the AfterLoginRegister and open the profile fragment
                Intent intent = new Intent(mContext, AfterLoginRegister.class);
                // Putting and sending a String with the Intent opening the Activity
                intent.putExtra("publisherid", comment.getPublisher());
                //Starting the Activity
                mContext.startActivity(intent);
            }
        });

        //Setting a click Listener for the image_profile to open the user profile
        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // creating an Intent to switch to the AfterLoginRegister and open the profile fragment
                Intent intent = new Intent(mContext, AfterLoginRegister.class);
                // Putting and sending a String with the Intent opening the Activity
                intent.putExtra("publisherid", comment.getPublisher());
                //Starting the Activity
                mContext.startActivity(intent);
            }
        });

        //Setting an OnLong String Listener to give the user a message to delete his/her comment
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // checking if the comment publisher is the same as the current user, if so Show the Delete Dialog.
                if (comment.getPublisher().equals(firebaseUser.getUid())){
                    // Creating the Alert Dialog
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    // Setting a Message for the Alert Dialog
                    alertDialog.setTitle("Are you sure you want to delete the comment?");
                    // Setting a button to disable the Alert Dialog when The user Clicks on No
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Disabling the Button
                            dialogInterface.dismiss();
                        }
                    });
                    // Setting a Positive Button to Delete the Comment when Th User Clicks on Ok
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Calling FirebaseDatabase to locate the Comment id and Delete the the data under the ID
                            FirebaseDatabase.getInstance().getReference("Comments")
                                    .child(postid).child(comment.getCommentid())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // If task is successful the User will get a positive message
                                    if (task.isSuccessful()){
                                        Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            // Dissmisses the Dialog when the all the process is done
                            dialogInterface.dismiss();
                        }
                    });
                    // Calling the Alert dialog to show
                    alertDialog.show();
                }
                //setting the OnLong Click listener to return true
                return true;
            }

        });

    }

    //automatically Generated method for the Adapter
    // This method return the Size of mComment List
    @Override
    public int getItemCount() {
        return mComment.size();
    }

    // automatically Generated child class for the Adapter
    // This class calls all the widgets located in the XML layout "comment_item"
    public class ViewHolder extends RecyclerView.ViewHolder{

        // Initializing the variables that will later be defined
        public CircleImageView image_profile;
        public TextView name, comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Defining the variables that where initialized to the IDs of the Widgets in the XML Layout
            image_profile = itemView.findViewById(R.id.image_profile);
            name = itemView.findViewById(R.id.name);
            comment = itemView.findViewById(R.id.comment);


        }
    }

    // this Method fetches the name and imageurl and of the User to be used in the onBindviewHolder method
    private void getUserInfo(final CircleImageView imageView, final TextView name, String publisherid){

        //Insializng user data reference from FirebaseDatabase.
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherid);

        //a Value listener for the variable reference
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Calling the User class and defining it with the DataSnapshot received from the reference
                User user = snapshot.getValue(User.class);
                //Loading the user Image into the imageView with Glide library.
                Glide.with(mContext).load(user.getImageurl()).into(imageView);
                // setting the name textView to the user name
                name.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
