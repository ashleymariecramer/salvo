package edu.example;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ashleymariecramer on 13/12/16.
 */
@Entity  // tells Java to create a 'GamePlayer' table for this class
public class GamePlayer {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private Date joinDate;
    //private long gameId;
    //private long playerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    public GamePlayer(){}

    public GamePlayer(long offset, Game game, Player player) {
        Date now = new Date();
        this.joinDate = Date.from(now.toInstant().plusSeconds(offset));
        this.game = game;
        this.player = player;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }


}