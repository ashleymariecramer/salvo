package edu.example;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by ashleymariecramer on 07/12/16.
 */

@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findPlayersByUsername(@Param("name") String name); //This one is used on SalvoApplication security
    Player findByUsername(@Param("name") String name); //this one for building /api/games DTO in SalvoController
    //Here findBy method needs to include the name of a parameter in the related class
    //but the name which is used after @Param can be anything - am using email
}
