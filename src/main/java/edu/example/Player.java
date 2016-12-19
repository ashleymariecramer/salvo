/**
 * Created by ashleymariecramer on 07/12/16.
 */

package edu.example;

import javax.persistence.*;
import java.util.Set;

@Entity  // tells Java to create a 'Player' table for this class
public class Player {

    @Id // id instance variable holds the database key for this class.
    @GeneratedValue(strategy=GenerationType.AUTO) // tells JPA to get the ID from the DBMS.
    private long id;
    private String nickname;
    private String username;

    public Player() { }

    public Player(String nickname, String email) {
        this.nickname = nickname;
        this.username = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String firstName) {
        this.nickname = firstName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String email) {
        this.username = email;
    }

    public void setId(long id) {
        this.id = id;
    }
    public long getId() {
        return id;
    }

    @OneToMany(mappedBy="player", fetch= FetchType.EAGER)
    Set<GamePlayer> gameplayers;

}
