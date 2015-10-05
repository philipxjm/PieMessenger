package com.choate.philip.pimessenger;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private MessageGetTask mGetMessageTask;
    public static String userData;
    public static String userName;
    private ArrayList<String> users;

    //private Button mSendButton;
    //private EditText mMessageHistory;
    //private EditText mMessageField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //System.out.println("activity oncreate");
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        System.out.println(intent.getStringExtra("USERDATA"));
        userData = intent.getStringExtra("USERDATA");
        userName = intent.getStringExtra("USERNAME");



        setContentView(R.layout.activity_chat);
        //getSupportFragmentManager().findFragmentById(R.id.navigation_drawer).setArguments(bundle);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));





        //mMessageHistory = (EditText) findViewById(R.id.messageHistory);
        //mMessageField = (EditText) findViewById(R.id.messageBox);
        //mSendButton = (Button) findViewById(R.id.button);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        try {
            JSONArray userData = new JSONArray(ChatActivity.userData);
            users = new ArrayList<>();
            for (int i = 0; i < userData.length(); i++) {
                try {
                    if(userData.get(i) instanceof JSONObject) {
                        if(!(((JSONObject) userData.get(i)).getString("name").equals(ChatActivity.userName))){
                            users.add(((JSONObject) userData.get(i)).getString("name"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mGetMessageTask = new MessageGetTask(userName, users.get(position), position);
        mGetMessageTask.execute("http://piemessengerbackend.herokuapp.com/messages/pull");
    }

    public void onSectionAttached(int number) {
        mTitle = mNavigationDrawerFragment.users.get(number - 1);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.chat, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        private Button mSendButton;
        private EditText mTextHistory;
        private EditText mTextBox;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, String userName) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString("USERNAME", userName);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

            mSendButton = (Button) rootView.findViewById(R.id.button);
            mTextBox = (EditText) rootView.findViewById(R.id.messageBox);
            mTextHistory = (EditText) rootView.findViewById(R.id.messageHistory);

            mSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Interface with database for message history
                    mTextHistory.setText(mTextHistory.getText().toString() + "\n" + mTextBox.getText());
                    mTextBox.setText("");
                }
            });

            return rootView;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            ((ChatActivity) context).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        public void addText(String text) {
            if(mTextHistory != null) {
                System.out.println("mTextHistory is null");
                mTextHistory.setText(mTextHistory.getText().toString() + "\n" + text);
            } else {
                //mTextHistory.setText(mTextHistory.getText().toString() + "\n" + text);
            }
        }
    }

    public class MessageGetTask extends AsyncTask<String, Void, Boolean> {
        JSONObject cred = new JSONObject();
        JSONObject returnObj;
        String from, to;
        int pos;

        MessageGetTask(String from, String to, int position) {
            this.from = from;
            this.to = to;
            pos = position;
            cred = new JSONObject();
            try {
                cred.put("from", from);
                cred.put("to", to);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            returnObj = null;
            try {
                HTTPRequestHandler http = new HTTPRequestHandler();
                System.out.println("\nSending Http POST request to: " + params[0]);
                try {
                    returnObj = http.getJSONFromUrl((String) params[0], cred);
                    System.out.println("JSON Object: " + returnObj.toString());
                    return true;
                } catch (Exception e) {
                    returnObj = new JSONObject("{}");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetMessageTask = null;

            if (success) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                PlaceholderFragment placeholderFragment = PlaceholderFragment.newInstance(pos + 1, from);

                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, placeholderFragment)
                        .commit();

                try {
                    JSONArray communications = returnObj.getJSONArray("communication");
                    for (int i = 0; i < communications.length(); i++) {
                        try {
                            if(communications.get(i) instanceof JSONObject) {
                                System.out.println(((JSONObject)communications.get(i)).getString("data"));
                                placeholderFragment.addText(((JSONObject)communications.get(i)).getString("data"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("onPostExecute of MessageGetTask failed");
            }
        }

        @Override
        protected void onCancelled() {
            mGetMessageTask = null;
        }
    }
}
