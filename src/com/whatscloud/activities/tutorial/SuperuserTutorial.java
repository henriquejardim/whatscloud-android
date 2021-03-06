package com.whatscloud.activities.tutorial;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockActivity;
import com.bugsense.trace.BugSenseHandler;
import com.whatscloud.R;
import com.whatscloud.config.reporting.BugSense;
import com.whatscloud.config.root.SuperSU;
import com.whatscloud.ui.dialogs.DialogManager;

public class SuperuserTutorial extends SherlockActivity
{
    Button mDone;
    ImageView mLaunchSuperUser;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        //---------------------------------
        // Call super
        //---------------------------------

        super.onCreate(savedInstanceState);

        //---------------------------------
        // Initialize bug tracking
        //---------------------------------

        BugSenseHandler.initAndStartSession(this, BugSense.API_KEY);

        //-----------------------------
        // Load UI elements
        //-----------------------------

        initializeUI();
    }

    void initializeUI()
    {
        //-----------------------------
        // Set default layout
        //-----------------------------

        setContentView(R.layout.superuser_tutorial);

        //-----------------------------
        // Find and cache UI elements
        //-----------------------------

        mDone = (Button)findViewById(R.id.done);
        mLaunchSuperUser = (ImageView)findViewById(R.id.launchSuperUser);

        //-----------------------------
        // Set up on click listeners
        //-----------------------------

        initializeListeners();
    }

    void initializeListeners()
    {
        //-----------------------------
        // Set up icon listener
        //-----------------------------

        mLaunchSuperUser.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //-----------------------------
                // Open SuperSU
                //-----------------------------

                launchSuperSu();
            }
        });

        //-----------------------------
        // Set up button listener
        //-----------------------------

        mDone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //-----------------------------
                // Open next tutorial
                //-----------------------------

                nextScreen();
            }
        });
    }

    void nextScreen()
    {
        //-----------------------------
        // Using Android 4.3 or later?
        //-----------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            //-----------------------------
            // Show notification access tut
            //-----------------------------

            startActivity(new Intent().setClass(this, NotificationAccessTutorial.class));
        }

        //-----------------------------
        // Exit this activity
        //-----------------------------

        finish();
    }

    void launchSuperSu()
    {
        try
        {
            //-----------------------------
            // Try to find SuperSU launch
            // intent action
            //-----------------------------

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(SuperSU.SUPERSU_PACKAGE);

            //-----------------------------
            // Start SuperSU activity
            //-----------------------------

            startActivity( launchIntent );
        }
        catch( Exception exc )
        {
            //-----------------------------
            // In case we fail
            //-----------------------------

            showDialog(DialogManager.SUPERUSER_FAIL);
        }
    }

    @Override
    protected Dialog onCreateDialog( int resource )
    {
        //---------------------------------
        // Create a dialog with error icon
        //---------------------------------

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_error)
                .setPositiveButton(getString(R.string.ok), null)
                .create();

        //-----------------------------
        // Build dialog message
        //-----------------------------

        DialogManager.BuildDialog(dialog, resource, this);

        //-----------------------------
        // Return dialog object
        //----------------------------

        return dialog;
    }
}
