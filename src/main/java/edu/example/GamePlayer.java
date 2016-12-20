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
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private LocalDateTime joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    private Game getGame() {
        return game;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    public Player getPlayer() {
        return player;
    }

    public GamePlayer(){}

    public GamePlayer(long offset, Game game, Player player) {
        Date now = new Date();
        //this.joinDate = Date.from(now.toInstant().plusSeconds(offset));
        this.joinDate = LocalDateTime.ofInstant(
                now.toInstant().plusSeconds(offset), ZoneId.systemDefault());
        this.game = game;
        this.player = player;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


}