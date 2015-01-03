package com.whatscloud.logic.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.whatscloud.config.app.WhatsCloud;
import com.whatscloud.config.debug.Logging;
import com.whatscloud.config.reporting.BugSense;
import com.whatscloud.logic.auth.User;
import com.whatscloud.logic.sync.SyncStatus;
import com.whatscloud.logic.sync.manager.SyncManager;
import com.whatscloud.utils.networking.HTTP;
import com.whatscloud.ui.dialogs.DialogManager;
import com.whatscloud.utils.strings.StringUtils;

public class PushReceiver extends BroadcastReceiver
{
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        //--------------------------------
        // Save context for later
        //--------------------------------

        this.mContext = context;

        //---------------------------------
        // Initialize bug tracking
        //---------------------------------

        BugSenseHandler.initAndStartSession(context, BugSense.API_KEY);

        //--------------------------------
        // Not logged in? Stop.
        //--------------------------------

        if (! User.isSignedIn(context) )
        {
            return;
        }

        //--------------------------------
        // Handle the push
        //--------------------------------

        handlePush(context, intent);
    }

    void handlePush(Context context, Intent intent)
    {
        //---------------------------------
        // Log the push
        //---------------------------------

        Log.d(Logging.TAG_NAME, "Received push");

        //--------------------------------
        // Get do
        //--------------------------------

        String method = intent.getStringExtra("do");

        //--------------------------------
        // No method, ignore
        //--------------------------------

        if (StringUtils.stringIsNullOrEmpty(method))
        {
            return;
        }

        //--------------------------------
        // Are we resetting unread count?
        //--------------------------------

        if ( method.equals("unread") )
        {
            //--------------------------------
            // Reset unread count
            //--------------------------------

            resetUnreadCount(context);
        }
        else
        {
            //--------------------------------
            // Get pending outgoing messages
            //--------------------------------

            new GetPendingChatMessagesAsync(context).execute();
        }

        //--------------------------------
        // Log the push
        //--------------------------------

        Log.d(Logging.TAG_NAME, "Received push: " + method);
    }

    void resetUnreadCount(Context context)
    {
        //--------------------------------
        // Call upon our sync manager
        //--------------------------------

        SyncManager manager = new SyncManager(context, false);

        //--------------------------------
        // Send the pending messages
        //--------------------------------

        try
        {
            manager.resetTotalUnreadCount();
        }
        catch( Exception exc )
        {
            //--------------------------------
            // Log the error
            //--------------------------------

            Log.e(Logging.TAG_NAME, exc.getMessage());
        }
    }

    void getPendingChatMessages(Context context) throws Exception
    {
        //--------------------------------
        // Get back pending messages
        //--------------------------------

        String responseJSON = HTTP.get(WhatsCloud.API_URL + "/messages?do=pending&key=" + User.getAPIKey(context));

        //--------------------------------
        // Call upon our sync manager
        //--------------------------------

        SyncManager manager = new SyncManager(context, false);

        //--------------------------------
        // Send messages
        //--------------------------------

        manager.sendPendingMessages(responseJSON);
    }

    public class GetPendingChatMessagesAsync extends AsyncTask<Long, String, Integer>
    {
        Context mContext;

        public GetPendingChatMessagesAsync(Context context)
        {
            this.mContext = context;
        }

        @Override
        protected Integer doInBackground(Long...parameter)
        {
            try
            {
                //--------------------------------
                // Currently syncing?
                // Please wait...
                //--------------------------------

                while ( SyncStatus.isSyncing(mContext) )
                {
                    Thread.sleep( 200 );
                }

                //--------------------------------
                // Set syncing to true to prevent
                // other process from syncing
                //--------------------------------

                SyncStatus.setSyncing(mContext, true);

                //--------------------------------
                // Try to receive the message
                //--------------------------------

                getPendingChatMessages(mContext);
            }
            catch( Exception exc )
            {
                //--------------------------------
                // Log the error
                //--------------------------------

                Log.e(Logging.TAG_NAME, exc.getMessage());

                //--------------------------------
                // Return error
                //--------------------------------

                return DialogManager.SYNC_FAILED;
            }

            //--------------------------------
            // Success!
            //--------------------------------

            return 0;
        }

        @Override
        protected void onPostExecute(Integer errorCode)
        {
            //--------------------------------
            // Set syncing to false
            //--------------------------------

            SyncStatus.setSyncing(mContext, false);
        }
    }
}