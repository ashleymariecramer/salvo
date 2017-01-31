/**
 * Created by ashleymariecramer on 07/12/16.
 */

package edu.example;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.Set;

@Entity  // tells Java to create a 'Player' table for this class
public class Player {

    //---------------------Properties(private)----------------------------------
    @Id // id instance variable holds the database key for this class.
    @GeneratedValue(strategy=GenerationType.AUTO) // tells JPA to get the ID from the DBMS.
    private long id;
    @NotEmpty (message = "Please enter a nickname")
    private String nickname;
    @NotEmpty (message = "Please enter a username")
    @Email (message = "Please enter valid email address")
    @Pattern(regexp=".+@.+\\..+", message="Please provide a valid email address") //checks email has format x@y.z
    private String username;
    @NotEmpty (message = "Please enter a password")
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
// Double validation - in Javascript & JAVA - checks nickname, username and password are not empty @Empty
// and also that the username has a correct email format @Email
// Can put these validation annotations either in the getters or in properties (above)

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String firstName) {
        this.nickname = firstName;
    }

    @Email (message = "Please enter valid email address")
    public String getUsername() {
        return username;
    }

    public void setUsername(String email) {
        this.username = email;
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
