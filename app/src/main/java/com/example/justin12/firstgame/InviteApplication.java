package com.example.justin12.firstgame;

import android.app.Application;
import com.facebook.model.GraphUser;
import java.util.List;

/**
 * Created by Justin12 on 1/20/2015.
 */
public class InviteApplication extends Application {
    private List<GraphUser> selectedUsers;

    //Player application attributes


    public List<GraphUser> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<GraphUser> users) {
        selectedUsers = users;
    }

}
