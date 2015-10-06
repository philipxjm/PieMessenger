package com.choate.philip.pimessenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle; //Title displayed on the actionbar
    private MessageGetTask mGetMessageTask; //AsyncTask for getting messages
    private MessagePostTask mPostMessageTask; //AsyncTask for pushing messages
    public static String userData; //JSONString of all the users
    public static String userName; //Who the user is
    public static String userTo; //Who the user is messaging
    public static int position; //which user are we talking to
    private ArrayList<String> users; //arraylist of all users

    @Override
    protected void onCreate(Bundle savedInstanceState) { //setting up the drawerfragments and receiving the intents extras
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
        mTitle = mNavigationDrawerFragment.users.get(0);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));





        //mMessageHistory = (EditText) findViewById(R.id.messageHistory);
        //mMessageField = (EditText) findViewById(R.id.messageBox);
        //mSendButton = (Button) findViewById(R.id.button);
    }

    @Override
    public void onNavigationDrawerItemSelected(int pos) { //called when a drawer item is selected
        // update the main content by replacing fragments
        position = pos;
        System.out.println("chatactivity onNavigationDrawerItemSelected");
        try {
            JSONArray userData = new JSONArray(ChatActivity.userData);
            users = new ArrayList<>();
            for (int i = 0; i < userData.length(); i++) { //setting up the local users array
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
        userTo = users.get(position);

        //dysfunctional timer for updating
        Timer timer = new Timer(true);
        TimerTask updateChatTask = new UpdateChatTask(position);
        updateChatTask.run();
        //timer.scheduleAtFixedRate(updateChatTask, 1000, 2000);
    }

    class UpdateChatTask extends TimerTask {
        int position;
        UpdateChatTask(int position) {
            this.position = position;
        }
        public void run() {
            mGetMessageTask = new MessageGetTask(userName, userTo, position);
            mGetMessageTask.execute("http://piemessengerbackend.herokuapp.com/messages/pull");
        }
    }

    public void onSectionAttached(int number) { //called when a section of drawer is attached
        mTitle = mNavigationDrawerFragment.users.get(number - 1);
    }

    public void restoreActionBar() { //sets up the actionbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //set up the menu of the actionbar
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
    public boolean onOptionsItemSelected(MenuItem item) { //sets up the actions of the buttons in actionbar
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            mGetMessageTask = new MessageGetTask(userName, userTo, position);
            mGetMessageTask.execute("http://piemessengerbackend.herokuapp.com/messages/pull");
            return true;
        }

        if (id == R.id.action_logout) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ValidFragment")
    public class PlaceholderFragment extends Fragment { //Chatfragment

        private static final String ARG_SECTION_NUMBER = "section_number";

        private Button mSendButton;
        private ListView mTextHistory;
        private EditText mTextBox;
        private JSONArray chatArray;
        private List<ChatMessage> chats;
        private ChatArrayAdapter chatArrayAdapter;

        public PlaceholderFragment (int sectionNumber, String userName) { //constructor
            this.chats = new ArrayList<ChatMessage>();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString("USERNAME", userName);
            this.setArguments(args);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, //sets up all the views
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

            mSendButton = (Button) rootView.findViewById(R.id.button);
            mTextBox = (EditText) rootView.findViewById(R.id.messageBox);
            mTextHistory = (ListView) rootView.findViewById(R.id.messageHistory);

            sortChats();
            chatArrayAdapter = new ChatArrayAdapter(getContext(), R.layout.right);
            mTextHistory.setAdapter(chatArrayAdapter);

            mTextBox.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        return sendChatMessage();
                    }
                    return false;
                }
            });
            mSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendChatMessage();
                }
            });

            mTextHistory.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            mTextHistory.setAdapter(chatArrayAdapter);

            chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    mTextHistory.setSelection(chatArrayAdapter.getCount() - 1);
                }
            });
            for(ChatMessage c : chats) {
                chatArrayAdapter.add(c);
            }

            return rootView;
        }

        public void sortChats() { //sort the chat array based on time
            Collections.sort(chats, new Comparator<ChatMessage>() {
                @Override
                public int compare(ChatMessage lhs, ChatMessage rhs) {
                    if (Long.valueOf(lhs.time) > Long.valueOf(rhs.time)) {
                        return 1;
                    } else if (Long.valueOf(lhs.time) < Long.valueOf(rhs.time)) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            ((ChatActivity) context).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        public void addChatMessage(boolean isTo, String text, String time) { //addes a chatmessage object to the chat array
            chats.add(new ChatMessage(isTo, text, time));
            sortChats();
        }

        public boolean sendChatMessage() { //push and display a new chatmessage
            if(mTextBox.getText().toString() == null || mTextBox.getText().toString().equals("")){
                return false;
            }
            mPostMessageTask = new MessagePostTask(userName, userTo, mTextBox.getText().toString(), System.currentTimeMillis() + "");
            mPostMessageTask.execute("http://piemessengerbackend.herokuapp.com/messages/push");
            chatArrayAdapter.add(new ChatMessage(true, mTextBox.getText().toString(), System.currentTimeMillis() + ""));
            mTextBox.setText("");
            sortChats();
            return true;
        }
    }

    public class MessageGetTask extends AsyncTask<String, Void, Boolean> { //retrieves chatmessage from cloud
        JSONObject cred1, cred2;
        JSONObject returnObj1, returnObj2, returnObj;
        String from, to;
        int pos;

        MessageGetTask(String from, String to, int position) {
            this.from = from;
            this.to = to;
            pos = position;
            cred1 = new JSONObject();
            cred2 = new JSONObject();
            try {
                cred1.put("from", from);
                cred1.put("to", to);
                cred2.put("from", to);
                cred2.put("to", from);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            returnObj1 = null;
            returnObj2 = null;
            try {
                HTTPRequestHandler http = new HTTPRequestHandler();
                System.out.println("\nSending Http POST request to: " + params[0]);
                try {
                    returnObj1 = http.getJSONFromUrl((String) params[0], cred1);
                    returnObj2 = http.getJSONFromUrl((String) params[0], cred2);

                    System.out.println("JSON Object1: " + returnObj1.toString());
                    System.out.println("JSON Object2: " + returnObj2.toString());
                    return true;
                } catch (Exception e) {
                    returnObj1 = new JSONObject("{}");
                    returnObj2 = new JSONObject("{}");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) { //add retrieved chatmessages to chats array
            mGetMessageTask = null;

            if (success) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                PlaceholderFragment placeholderFragment = new PlaceholderFragment(pos + 1, from);

                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, placeholderFragment)
                        .commit();

                try {
                    JSONArray communications1 = returnObj1.getJSONArray("communication");
                    for (int i = 0; i < communications1.length(); i++) {
                        try {
                            if(communications1.get(i) instanceof JSONObject) {
                                //System.out.println(((JSONObject)communications1.get(i)).getString("data"));
                                placeholderFragment.addChatMessage(true, ((JSONObject) communications1.get(i)).getString("data"), ((JSONObject) communications1.get(i)).getString("time"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    JSONArray communications2 = returnObj2.getJSONArray("communication");
                    for (int i = 0; i < communications2.length(); i++) {
                        try {
                            if(communications2.get(i) instanceof JSONObject) {
                                //System.out.println(((JSONObject) communications2.get(i)).getString("data"));
                                placeholderFragment.addChatMessage(false, ((JSONObject) communications2.get(i)).getString("data"), ((JSONObject) communications2.get(i)).getString("time"));
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

    public class MessagePostTask extends AsyncTask<String, Void, Boolean> { //task for pushing new data onto cloud
        JSONObject jobj = new JSONObject();

        MessagePostTask(String from, String to, String message, String time) {
            jobj = new JSONObject();
            try {
                jobj.put("from", from);
                jobj.put("to", to);
                JSONObject newCommunication = new JSONObject();
                newCommunication.put("time", time);
                newCommunication.put("data", message);
                jobj.put("newCommunication", newCommunication);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            JSONObject returnObj = null;
            try {
                HTTPRequestHandler http = new HTTPRequestHandler();
                System.out.println("\nSending Http POST request to: " + params[0]);
                try {
                    returnObj = http.getJSONFromUrl((String) params[0], jobj);
                } catch (Exception e) {
                    returnObj = new JSONObject("{\"status\" : \"false\"}");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if(returnObj != null){
                    return returnObj.getBoolean("status");
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                System.out.println("success pushing chat");
            } else {
                System.out.println("failure pushing chat");
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    class ChatArrayAdapter extends ArrayAdapter<ChatMessage> { //adapter for the listview that displays all the chats

        private TextView chatText;
        private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
        private Context context;

        @Override
        public void add(ChatMessage object) {
            chatMessageList.add(object);
            super.add(object);
        }

        public ChatArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.context = context;
        }

        public int getCount() {
            return this.chatMessageList.size();
        }

        public ChatMessage getItem(int index) {
            return this.chatMessageList.get(index);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ChatMessage chatMessageObj = getItem(position);
            View row = convertView;
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (chatMessageObj.to) {
                row = inflater.inflate(R.layout.right, parent, false);
            } else {
                row = inflater.inflate(R.layout.left, parent, false);
            }
            chatText = (TextView) row.findViewById(R.id.msg);
            chatText.setText(chatMessageObj.message);
            return row;
        }
    }

    class ChatMessage { //ChatMessage
        public boolean to;
        public String message;
        public String time;

        public ChatMessage(boolean left, String message, String time) {
            super();
            this.time = time;
            this.to = left;
            this.message = message;
        }
    }
}
