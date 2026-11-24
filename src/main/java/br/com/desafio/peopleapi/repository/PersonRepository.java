package br.com.desafio.peopleapi.repository;

import br.com.desafio.peopleapi.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByDocument(String document);
    boolean existsByEmail(String email);
}
