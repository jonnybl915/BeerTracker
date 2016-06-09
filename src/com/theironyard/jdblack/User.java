package com.theironyard.jdblack;

import com.theironyard.jdblack.Beer;

import java.util.ArrayList;

/**
 * Created by jonathandavidblack on 6/9/16.
 */
public class User {
    String username;
    String password;
    ArrayList<Beer> beerList = new ArrayList<>();

    public User(String username, String password, ArrayList<Beer>beerList) {
        this.username = username;
        this.password = password;
        this.beerList = beerList;
    }
}
