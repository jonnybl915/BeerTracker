package com.theironyard.jdblack;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import static spark.Spark.halt;

public class Main {

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
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM beers INNER JOIN users ON beers.user_id = users.id WHERE beers.id = ?");
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
            int beerId = results.getInt("beers.id");
            String beerName = results.getString("beers.beerName");
            String breweryName = results.getString("beers.breweryName");
            String beerStyle = results.getString("beers.beerStyle");
            int abv = results.getInt("beers.abv");
            String comment = results.getString("beers.comment");
            Beer beer = new Beer(beerId, beerName, breweryName, beerStyle, abv, comment);
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
                    String password = session.attribute("password");

                    HashMap map = new HashMap();
                    if (username == null){
                        return new ModelAndView(map, "login.html"); //maybe response.redirect
                    }
                    else {
                        User user = selectUser(conn, username);
                        ArrayList<Beer> beerList = selectBeers(conn, user.id);
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
                    User user = selectUser(conn, name);
                    if (user == null) {

                        insertUser(conn, name, pass);
                    }
                    else if(!pass.equals(user.password)) {
                        halt("Incorrect Username/Password Combination.\n" +
                                "Please Go Back");
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
                    User user = selectUser(conn, username);
                    if(username == null){
                        throw new Exception("you must log in first");
                    }
                    String beerName = request.queryParams("beerName");
                    String breweryName = request.queryParams("breweryName");
                    String beerStyle = request.queryParams("beerStyle");
                    int abv = Integer.valueOf(request.queryParams("abv"));
                    String comment = request.queryParams("comment");

                    insertBeer(conn, beerName, breweryName, beerStyle, abv, comment, user.id);
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/delete-entryItem",
                (request, response) -> {

                    Session session = request.session();
                    String username = session.attribute("username");

                    if(username == null){
                        throw new Exception("you must log in first");
                    }
                    int id = Integer.valueOf(request.queryParams("id"));

                    deleteBeer(conn, id);

                    response.redirect("/");
                    return "";
                }
        );
        Spark.get(
                "/edit-entry",
                (request, response) -> {

                    Session session = request.session();
                    String username = session.attribute("username");

                   // User user = selectUser(conn, username);
                    if(username == null) {
                        throw new Exception("you must log in first");
                    }
                    int id = (Integer.valueOf(request.queryParams("id")));
                    HashMap map = new HashMap();
                    Beer beer = selectBeer(conn, id);
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
                    User user = selectUser(conn, username);
                    String beerName = request.queryParams("beerName");
                    String breweryName = request.queryParams("breweryName");
                    String beerStyle = request.queryParams("beerStyle");
                    int abv = Integer.valueOf(request.queryParams("abv"));
                    String comment = request.queryParams("comment");
                    int id = Integer.valueOf(request.queryParams("id"));
                    if(username == null) {
                        throw new Exception("you must log in first");
                    }

                    updateBeer(conn, beerName, breweryName, beerStyle, abv, comment, id);
                    response.redirect("/");
                    return "";
                }
        );
    }
}
