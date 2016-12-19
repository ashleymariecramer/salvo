/**
 * Created by ashleymariecramer on 07/12/16.
 */

package edu.example;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Entity  // tells Java to create a 'Game' table for this class
public class Game {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private Date creationDate;

    public Game() { }

    public Game(long offset) {
        Date now = new Date();
        this.creationDate = Date.from(now.toInstant().plusSeconds(offset));
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }


    @OneToMany(mappedBy="game", fetch= FetchType.EAGER)
    Set<GamePlayer> gameplayers; //Collection of Objects of type GamePlayer - 'gameplayers' is the name I've given to this collection.
    //A set which contains objects from the class GamePlayer
    //when you call the method getGamePlayers() it returns the collection of objects 'gameplayers'
    public Set<GamePlayer> getGamePlayers() { return gameplayers; }

}



