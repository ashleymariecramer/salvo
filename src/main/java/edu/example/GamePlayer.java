package edu.example;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;


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
    @OneToMany(mappedBy="gamePlayer", fetch = FetchType.EAGER) //linking ship id to a gamePlayer like games to gamePlayer
    private Set<Ship> ship; //Collection of Objects of type GamePlayer
    @OneToMany(mappedBy="gamePlayer", fetch = FetchType.EAGER) //linking salvo id to a gamePlayer like games to gamePlayer
    private Set<Salvo> salvo; //Collection of Objects of type GamePlayer


    // ---------------------Constructors(public)----------------------------------
    public GamePlayer(){}

    public GamePlayer(long offset, Game game, Player player) {
        Date now = new Date();
        this.joinDate = Date.from(now.toInstant().plusSeconds(offset));
        this.game = game;
        this.player = player;
    }

    //this is second constructor which takes as a default offset 0, if new GamePlayer(1, 2) is passed
    // with only 2 parameters (instead of 3 in the constructor above) then Java knows to use this version.
    public GamePlayer(Game game, Player player) {
        this(0l, game, player);
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

    public void setShip(Set<Ship>  ship) {
        this.ship = ship;
    }
    public Set<Ship> getShip() {
        return ship;
    }

    public Set<Salvo> getSalvo() {
        return salvo;
    }

    public void setSalvo(Set<Salvo> salvo) {
        this.salvo = salvo;
    }




}