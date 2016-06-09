package com.theironyard.jdblack;

/**
 * Created by jonathandavidblack on 6/9/16.
 */
public class Beer {
    String beerName;
    String breweryName;
    String beerStyle;
    int abv;
    int ibu;
    String comment;

    public Beer(String beerName, String breweryName, String beerStyle, int abv, int ibu, String comment) {
        this.beerName = beerName;
        this.breweryName = breweryName;
        this.beerStyle = beerStyle;
        this.abv = abv;
        this.ibu = ibu;
        this.comment = comment;
    }
}
