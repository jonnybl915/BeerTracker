package com.theironyard.jdblack;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
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
                        return new ModelAndView(map, "login.html"); //maybe response.redirect
                    }
                    else {
                        ArrayList<Beer> beerList = userList.get(username).beerList;
                        map.put("name", username);
                        map.put("beers", beerList);
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
        Spark.post(
                "/logout",
                (request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/create-entry",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userList.get(username);
                    if(username == null){
                        throw new Exception("you must log in first");
                    }
                    Beer beer = new Beer();
                    beer.setBeerName(request.queryParams("beerName"));
                    beer.setBreweryName(request.queryParams("breweryName"));
                    beer.setBeerStyle(request.queryParams("beerStyle"));
                    beer.setAbv(Float.valueOf(request.queryParams("abv"))); //need to allow for this field to be empty
                    beer.setComment(request.queryParams("comment"));
                    beer.setId(user.beerList.size());
                    user.beerList.add(beer);
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/delete-entryItem",
                (request, response) -> {
                    int id = (Integer.valueOf(request.queryParams("id")));

                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userList.get(username);
                    if(username == null){
                        throw new Exception("you must log in first");
                    }
                    user.beerList.remove(id);

                    int indexValue = 0;
                    for (Beer beer : user.beerList){
                        beer.setId(indexValue);
                        indexValue++;
                    }
                    response.redirect("/");
                    return "";
                }
        );
        Spark.get(
                "/edit-entry",
                (request, response) -> {

                    Session session = request.session();
                    String username = session.attribute("username");

                    User user = userList.get(username);
                    if(username == null) {
                        throw new Exception("you must log in first");
                    }
                    int id = (Integer.valueOf(request.queryParams("id")));
                    HashMap map = new HashMap();
                    Beer beer = user.beerList.get(id);
                    map.put("beer", beer);

                    return new ModelAndView(map, "updateBeer.html");
                },
                new MustacheTemplateEngine()
        );
        Spark.patch(
                "/update-entry",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userList.get(username);
                    if(username == null){
                        throw new Exception("you must log in first");
                    }
                    int id = (Integer.valueOf(request.queryParams("id")));
                    user.beerList.get(id);

//                    String editBeerName = request.queryParams("newBeerName");
//                    String editBreweryName = request.queryParams("newBreweryName");
//                    String editBeerStyle = request.queryParams("newBeerStyle");
//                    float editAbv = Float.valueOf(request.queryParams("newAbv"));
//                    String editComment = request.queryParams("newComment");
//                    user.beerList.set(id, new Beer(editBeerName, editBreweryName, editBeerStyle, editAbv, editComment, id));
                    response.redirect(request.headers("Referer"));
                    return "";
                }
        );
    }
}
