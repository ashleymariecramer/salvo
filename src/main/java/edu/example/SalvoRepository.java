package edu.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by ashleymariecramer on 22/12/16.
 */

@RepositoryRestResource
public interface SalvoRepository extends JpaRepository<Salvo, Long> {

}
