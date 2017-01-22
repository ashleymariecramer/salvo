/**
 * Created by ashleymariecramer on 07/12/16.
 * Correct order is: properties, constructors, getters & setters (group together get & set for each property)
 */

package edu.example;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;


@Entity  // tells Java to create a 'Game' table for this class
public class Game {

    //---------------------Properties(private)----------------------------------
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private Date creationDate;
    @OneToMany(mappedBy="game", fetch= FetchType.EAGER)
    private Set<GamePlayer> gamePlayers; //Collection of Objects of type GamePlayer - 'gamePlayers' is the name I've given to this collection.
    //A set which contains objects from the class GamePlayer
    @OneToMany(mappedBy="game", fetch= FetchType.EAGER)
    private Set<GameScore> gameScores; //Collection of Objects of type GameScore - 'gameScores' is the name I've given to this collection.
    //A set which contains objects from the class GameScore

    // ---------------------Constructors(public)----------------------------------
    public Game() { }

    public Game(long offset) {
        Date now = new Date();
        this.creationDate = Date.from(now.toInstant().plusSeconds(offset));
    }

    // ---------------------Methods(public)----------------------------------
    public long getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    //when you call the method getGamePlayers() it returns the collection of objects 'gamePlayers'
    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    //when you call the method getGamePlayers() it returns the collection of objects 'gameScores'
    public Set<GameScore> getGameScores() {
        return gameScores;
    }




}



