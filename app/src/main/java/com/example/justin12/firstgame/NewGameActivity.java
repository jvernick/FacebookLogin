package com.example.justin12.firstgame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class NewGameActivity extends Activity implements View.OnClickListener {

    String TAG = "NewGameActivity";

    private ListView listView;
    private List<BaseListElement> listElements;
    private List<GraphUser> selectedUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        // Find the list view
        listView = (ListView) findViewById(R.id.selection_list);
        Button submit = (Button) findViewById(R.id.submitButton);
        submit.setOnClickListener(this);
// Set up the list view items, based on a list of
// BaseListElement items
        listElements = new ArrayList<BaseListElement>();
// Add an item for the friend picker
        listElements.add(new PeopleListElement(0));
// Set the list view adapter
        listView.setAdapter(new ActionListAdapter(this,
                R.id.selection_list, listElements));

    }


    @Override
    public void onClick(View v) {
        //do what you want to do when button is clicked
        switch (v.getId()) {
            case R.id.submitButton:
                Intent intent = new Intent();
                intent.setClassName("com.example.justin12.firstgame", "com.example.justin12.firstgame.GameActivity");
                startActivity(intent);
                break;
        }
    }

    private class ActionListAdapter extends ArrayAdapter<BaseListElement> {
        private List<BaseListElement> listElements;

        public ActionListAdapter(Context context, int resourceId,
                                 List<BaseListElement> listElements) {
            super(context, resourceId, listElements);
            this.listElements = listElements;
            // Set up as an observer for list item changes to
            // refresh the view.
            for (int i = 0; i < listElements.size(); i++) {
                listElements.get(i).setAdapter(this);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater =
                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.listitem, null);
            }

            BaseListElement listElement = listElements.get(position);
            if (listElement != null) {
                view.setOnClickListener(listElement.getOnClickListener());
                ImageView icon = (ImageView) view.findViewById(R.id.icon);
                TextView text1 = (TextView) view.findViewById(R.id.text1);
                TextView text2 = (TextView) view.findViewById(R.id.text2);
                if (icon != null) {
                    icon.setImageDrawable(listElement.getIcon());
                }
                if (text1 != null) {
                    text1.setText(listElement.getText1());
                }
                if (text2 != null) {
                    text2.setText(listElement.getText2());
                }
            }
            return view;
        }

    }

    private class PeopleListElement extends BaseListElement {
        private List<GraphUser> selectedUsers;
        private static final String FRIENDS_KEY = "friends";

        public PeopleListElement(int requestCode) {
            super(getResources().getDrawable(R.drawable.add_food),
                    getResources().getString(R.string.action_people),
                    getResources().getString(R.string.action_people_default),
                    requestCode);
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startPickerActivity(PickerActivity.FRIEND_PICKER, getRequestCode());
                }
            };
        }

        private void setUsersText() {
            String text = null;
            if (selectedUsers != null) {
                // If there is one friend
                if (selectedUsers.size() == 1) {
                    text = String.format(getResources()
                                    .getString(R.string.single_user_selected),
                            selectedUsers.get(0).getName());
                } else if (selectedUsers.size() == 2) {
                    // If there are two friends
                    text = String.format(getResources()
                                    .getString(R.string.two_users_selected),
                            selectedUsers.get(0).getName(),
                            selectedUsers.get(1).getName());
                } else if (selectedUsers.size() > 2) {
                    // If there are more than two friends
                    text = String.format(getResources()
                                    .getString(R.string.multiple_users_selected),
                            selectedUsers.get(0).getName(),
                            (selectedUsers.size() - 1));
                }
            }
            if (text == null) {
                // If no text, use the placeholder text
                text = getResources()
                        .getString(R.string.action_people_default);
            }
            // Set the text in list element. This will notify the
            // adapter that the data has changed to
            // refresh the list view.
            setText2(text);
        }

        private void addPlayersToInvite() {
            TextView player1 = (TextView) findViewById(R.id.player_name1);
            TextView player2 = (TextView) findViewById(R.id.player_name2);
            TextView player3 = (TextView) findViewById(R.id.player_name3);
            player1.setText(selectedUsers.get(0).getName());
            player2.setText(selectedUsers.get(1).getName());
            player3.setText(selectedUsers.get(2).getName());

        }

        @Override
        protected void onActivityResult(Intent data) {
            selectedUsers = ((InviteApplication) getApplication())
                    .getSelectedUsers();
            //addPlayersToInvite();
            //setUsersText();
            notifyDataChanged();
        }

        private byte[] getByteArray(List<GraphUser> users) {
            // convert the list of GraphUsers to a list of String
            // where each element is the JSON representation of the
            // GraphUser so it can be stored in a Bundle
            List<String> usersAsString = new ArrayList<String>(users.size());

            for (GraphUser user : users) {
                usersAsString.add(user.getInnerJSONObject().toString());
            }
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                new ObjectOutputStream(outputStream).writeObject(usersAsString);
                return outputStream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, "Unable to serialize users.", e);
            }
            return null;
        }

        @Override
        protected void onSaveInstanceState(Bundle bundle) {
            if (selectedUsers != null) {
                bundle.putByteArray(FRIENDS_KEY,
                        getByteArray(selectedUsers));
            }
        }

        private List<GraphUser> restoreByteArray(byte[] bytes) {
            try {
                @SuppressWarnings("unchecked")
                List<String> usersAsString =
                        (List<String>) (new ObjectInputStream
                                (new ByteArrayInputStream(bytes)))
                                .readObject();
                if (usersAsString != null) {
                    List<GraphUser> users = new ArrayList<GraphUser>
                            (usersAsString.size());
                    for (String user : usersAsString) {
                        GraphUser graphUser = GraphObject.Factory
                                .create(new JSONObject(user),
                                        GraphUser.class);
                        users.add(graphUser);
                    }
                    return users;
                }
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Unable to deserialize users.", e);
            } catch (IOException e) {
                Log.e(TAG, "Unable to deserialize users.", e);
            } catch (JSONException e) {
                Log.e(TAG, "Unable to deserialize users.", e);
            }
            return null;
        }

        @Override
        protected boolean restoreState(Bundle savedState) {
            byte[] bytes = savedState.getByteArray(FRIENDS_KEY);
            if (bytes != null) {
                selectedUsers = restoreByteArray(bytes);
                //setUsersText();
                return true;
            }
            return false;
        }
    }

    private void startPickerActivity(Uri data, int requestCode) {
        Intent intent = new Intent();
        intent.setData(data);
        intent.setClass(this, PickerActivity.class);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK &&
                requestCode >= 0 && requestCode < listElements.size()) {
            listElements.get(requestCode).onActivityResult(data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        for (BaseListElement listElement : listElements) {
            listElement.onSaveInstanceState(bundle);
        }
        //uiHelper.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
