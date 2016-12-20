package edu.example;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


/**
 * Created by ashleymariecramer on 13/12/16.
 */
@Entity  // tells Java to create a 'GamePlayer' table for this class
public class GamePlayer {

    //---------------------Properties(private)----------------------------------
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private Date joinDate;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    // ---------------------Constructors(public)----------------------------------
    public GamePlayer(){}

    public GamePlayer(long offset, Game game, Player player) {
        Date now = new Date();
        this.joinDate = Date.from(now.toInstant().plusSeconds(offset));
        //this.joinDate = LocalDateTime.ofInstant(
        //        now.toInstant().plusSeconds(offset), ZoneId.systemDefault());
        this.game = game;
        this.player = player;
    }

    // ---------------------Methods(public)----------------------------------
    public long getId() {
        return id;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}