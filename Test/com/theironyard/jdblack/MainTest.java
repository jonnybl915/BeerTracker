package com.theironyard.jdblack;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by jonathandavidblack on 6/14/16.
 */
public class MainTest {

    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Jonny", "");
        User user = Main.selectUser(conn, "Jonny");
        conn.close();
        assertTrue(user != null);
    }

    @Test
    public void testBeer() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertBeer(conn, "testBeerName", "testBreweryName", "testBeerStyle", 5, "testComment", 1);
        Beer testBeer = Main.selectBeer(conn, 1);
        conn.close();
        assertTrue(testBeer != null);
    }

    @Test
    public void testSelect() throws SQLException {
        Connection conn = startConnection();

        Main.insertUser(conn, "Alice", "");
        Main.insertUser(conn, "Bob", "");

        User alice = Main.selectUser(conn, "Alice");
        User bob = Main.selectUser(conn, "Bob");

        Main.insertBeer(conn, "testName", "testBrewery", "testStyle", 5, "testComment", alice.id);
        Main.insertBeer(conn, "testName2", "testBrewery2", "testStyle2", 3, "testComment2", bob.id);
        Main.insertBeer(conn, "testName3", "testBrewery3", "testStyle3", 8, "testComment3", bob.id);

        ArrayList<Beer> testList = Main.selectBeers(conn, 2);
        conn.close();
        assertTrue(testList.size() == 2);
    }

    @Test
    public void testUpdate() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Charlie", "");

        Main.insertBeer(conn, "testName", "testBrewery", "testStyle", 5, "testComment", 1);
        Main.updateBeer(conn, "newName", "newBrewery", "testStyle", 3, "newComment", 1);
        Beer testBeer = Main.selectBeer(conn, 1);
        conn.close();
        assertTrue(testBeer.beerName.equals("newName"));
        assertTrue(testBeer.breweryName.equals("newBrewery"));
    }

    @Test
    public void testDeleteMessage() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertBeer(conn, "testName", "testBrewery", "testStyle", 5, "testComment", 1);
        Main.deleteBeer(conn, 1);
        Beer beer = Main.selectBeer(conn, 1);
        conn.close();
        assertTrue(beer == null);
    }
}