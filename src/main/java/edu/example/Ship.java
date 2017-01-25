package edu.example;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ashleymariecramer on 22/12/16.
 */
@Entity  // tells Java to create a 'Ship' table for this class
public class Ship {

    //---------------------Properties(private)----------------------------------
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private String type; //e.g. Airplane Carrier(5), Battleship(4), Submarine(3), Destroyer(3), Patrol Boat(2)

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="gameplayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection  // As Ship locations only contains list of strings (eg. "H1", "H2", "H3") no need to create new class for this
    @Column(name="locations")
    private List<String> locations;

    // ---------------------Constructors(public)----------------------------------
    public Ship() { }

    public Ship(String type, GamePlayer gamePlayer, List<String> locations) {
        this.type = type;
        this.locations = locations;
        this.gamePlayer = gamePlayer;
    }


    // ---------------------Methods(public)----------------------------------
    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }



}
