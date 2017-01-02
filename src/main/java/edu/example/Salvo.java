package edu.example;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ashleymariecramer on 22/12/16.
 */
@Entity  // tells Java to create a 'Salvo' table for this class
public class Salvo {

    //---------------------Properties(private)----------------------------------
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private int turn;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="gameplayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection  // As Salvo locations only contains list of strings (eg. "H1", "H2", "H3") no need to create new class for this
    @Column(name="locations")
    private List<String> locations;

    // ---------------------Constructors(public)----------------------------------
    public Salvo() { }

    public Salvo(int turn, GamePlayer gamePlayer, List<String> locations) {
        this.turn = turn;
        this.locations = locations;
        this.gamePlayer = gamePlayer;
    }

    // ---------------------Methods(public)----------------------------------
    public long getId() {
        return id;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(String type) {
        this.turn = turn;
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