package com.theironyard.jdblack;

/**
 * Created by jonathandavidblack on 6/9/16.
 */
public class Beer {
    String beerName;
    String breweryName;
    String beerStyle;
    float abv;
    String comment;

    public Beer(String beerName, String breweryName, String beerStyle, float abv, String comment) {
        this.beerName = beerName;
        this.breweryName = breweryName;
        this.beerStyle = beerStyle;
        this.abv = abv;
        this.comment = comment;
    }

    public Beer() {

    }

    public String getBeerName() {
        return beerName;
    }

    public void setBeerName(String beerName) {
        this.beerName = beerName;
    }

    public String getBreweryName() {
        return breweryName;
    }

    public void setBreweryName(String breweryName) {
        this.breweryName = breweryName;
    }

    public String getBeerStyle() {
        return beerStyle;
    }

    public void setBeerStyle(String beerStyle) {
        this.beerStyle = beerStyle;
    }

    public float getAbv() {
        return abv;
    }

    public void setAbv(float abv) {
        this.abv = abv;
    }
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
