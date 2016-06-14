package com.theironyard.jdblack;

import com.theironyard.jdblack.Beer;

import java.util.ArrayList;

/**
 * Created by jonathandavidblack on 6/9/16.
 */
public class User {
    int id;
    String username;
    String password;
    ArrayList<Beer> beerList = new ArrayList<>();

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
