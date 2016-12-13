/**
 * Created by ashleymariecramer on 07/12/16.
 */

package edu.example;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

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

    @OneToMany(mappedBy="game", fetch= FetchType.EAGER)
    Set<GamePlayer> gameplayers;
}



