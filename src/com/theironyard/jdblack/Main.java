package com.theironyard.jdblack;

import org.h2.tools.Server;
import org.h2.util.Permutations;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> userList = new HashMap<>();

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, username VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS beers(id IDENTITY, beerName VARCHAR, breweryName VARCHAR, beerStyle VARCHAR, abv INT, comment VARCHAR, user_id INT)");
    }
    public static void insertUser(Connection conn, String username, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, username);
        stmt.setString(2, password);
        stmt.execute();
    }
    public static User selectUser(Connection conn, String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            String password = results.getString("password");
            return new User(id, username, password);
        }
        return null;
    }
    public static void insertBeer(Connection conn, String beerName, String breweryName, String beerStyle, int abv, String comment, int user_id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers VALUES (NULL, ?, ?, ?, ?, ?, ?)");
        stmt.setString(1, beerName);
        stmt.setString(2, breweryName);
        stmt.setString(3, beerStyle);
        stmt.setInt(4, abv);
        stmt.setString(5, comment);
        stmt.setInt(6, user_id);
        stmt.execute();
    }
    public static Beer selectBeer(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM beers INNER JOIN users ON beers.user_id = users.id WHERE users.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            String beerName = results.getString("beers.beerName");
            String breweryName = results.getString("beers.breweryName");
            String beerStyle = results.getString("beers.beerStyle");
            int abv = results.getInt("beers.abv");
            String comment = results.getString("beers.comment");
            return new Beer(id, beerName, breweryName, beerStyle, abv, comment);
        }
        return null;
    }
    public static ArrayList<Beer> selectBeers(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM beers INNER JOIN users ON beers.user_id = users.id WHERE beers.user_id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        ArrayList<Beer> beerList = new ArrayList<>();
        while (results.next()) {
            String beerName = results.getString("beers.beerName");
            String breweryName = results.getString("beers.breweryName");
            String beerStyle = results.getString("beers.beerStyle");
            int abv = results.getInt("beers.abv");
            String comment = results.getString("beers.comment");
            Beer beer = new Beer(id, beerName, breweryName, beerStyle, abv, comment);
            beerList.add(beer);
        }
        return beerList;
    }
    public static Beer updateBeer(Connection conn, String beerName, String breweryName, String beerStyle, int abv, String comment, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("Update beers SET beerName = ?, breweryName = ?, beerStyle = ?, abv = ?, comment = ? WHERE id = ?");
        stmt.setString(1, beerName);
        stmt.setString(2, breweryName);
        stmt.setString(3, beerStyle);
        stmt.setInt(4, abv);
        stmt.setString(5, comment);
        stmt.setInt(6, id);
        stmt.execute();
        return new Beer(id, beerName, breweryName, beerStyle, abv, comment);
    }

    public static void deleteBeer(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }



    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");

        Spark.staticFileLocation("public");
        Spark.init();
        createTables(conn);

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
                    if (name == null || pass == null || name.isEmpty() || pass.isEmpty()){
                        HashMap map = new HashMap();
                        return new ModelAndView(map, "login.html");
                    }
                    User user = userList.get(name);
                    if (user == null) {
                        user = new User(name, pass);
                        userList.put(name, user);
                    }
                    else if(!pass.equals(user.password)) {
                        HashMap map = new HashMap();
                        return new ModelAndView(map, "login.html"); //make this redirect***
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
        Spark.post(
                "/update-entry",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = userList.get(username);
                    if(username == null) {
                        throw new Exception("you must log in first");
                    }
                    int id = Integer.valueOf(request.queryParams("id"));
                    Beer beer = user.beerList.get(id);
                    beer.setBeerName(request.queryParams("newBeerName"));
                    beer.setBreweryName(request.queryParams("newBreweryName"));
                    beer.setBeerStyle(request.queryParams("newBeerStyle"));
                    beer.setAbv(Float.valueOf(request.queryParams("newAbv")));
                    beer.setComment(request.queryParams("newComment"));
                    response.redirect("/");
                    return "";
                }
        );
    }
}
