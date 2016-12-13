package edu.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Date;
import java.util.List;

/**
 * Created by ashleymariecramer on 07/12/16.
 */

@RepositoryRestResource
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
    //List<GamePlayer> findByJoinDate(Date joinDate);

}