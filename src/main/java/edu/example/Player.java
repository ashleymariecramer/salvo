/**
 * Created by ashleymariecramer on 07/12/16.
 */

package edu.example;

import javax.persistence.*;
import java.util.Set;

@Entity  // tells Java to create a 'Player' table for this class
public class Player {

    //---------------------Properties(private)----------------------------------
    @Id // id instance variable holds the database key for this class.
    @GeneratedValue(strategy=GenerationType.AUTO) // tells JPA to get the ID from the DBMS.
    private long id;
    private String nickname;
    private String username;
    private String password;
    @OneToMany(mappedBy="player", fetch= FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;
    @OneToMany(mappedBy="player", fetch= FetchType.EAGER)
    private Set<GameScore> gameScores;

    // ---------------------Constructors(public)----------------------------------
    public Player() { }

    public Player(String nickname, String email, String password) {
        this.nickname = nickname;
        this.username = email;
        this.password = password;
    }

    // ---------------------Methods(public)----------------------------------
    public void setNickname(String firstName) {
        this.nickname = firstName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setUsername(String email) {
        this.username = email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public Set<GameScore> getGameScores() {
        return gameScores;
    }


}
