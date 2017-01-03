package edu.example;

import javax.persistence.*;
import java.util.Date;


/**
 * Created by ashleymariecramer on 03/01/17.
 */
@Entity  // tells GameScore to create a 'Salvo' table for this class
public class GameScore {

    //---------------------Properties(private)----------------------------------
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private Date finishDate;
    private double score;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;


    // ---------------------Constructors(public)----------------------------------
    public GameScore() { }

    public GameScore(long offset, Game game, Player player, double score) {
        Date now = new Date();
        this.finishDate = Date.from(now.toInstant().plusSeconds(offset));
        this.score = score;
        this.player = player;
        this.game = game;
    }

    // ---------------------Methods(public)----------------------------------
    public long getId() {
        return id;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }


}

