package com.theironyard.jdblack;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> userList = new HashMap<>();

    public static void main(String[] args) {
        Spark.init();
        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    HashMap map = new HashMap();
                    if (username == null){
                        return new ModelAndView(map, "login.html");
                    }
                    else {
                        User user = userList.get(username);
                        map.put("name", username);
                        map.put("beers", user.beerList);
                        return new ModelAndView(map, "beerList.html");
                    }
                },
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                (request, response) -> {
                    String name = request.queryParams("username");
                    String pass = request.queryParams("password");
                    if (name == null || pass == null){
                        response.redirect("/");
                    }
                    User user = userList.get(name);
                    if (user == null) {
                        user = new User(name, pass);
                        userList.put(name, user);
                    }
                    else if(!pass.equals(user.password)) {
                        throw new Exception("Wrong Password"); //make this redirect***
                    }
                    Session session = request.session();
                    session.attribute("username", name);

                    response.redirect("/");
                    return "";
                }
        );
    }
}
