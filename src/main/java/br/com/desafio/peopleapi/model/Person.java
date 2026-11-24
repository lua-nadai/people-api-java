package br.com.desafio.peopleapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "people", uniqueConstraints = {
    @UniqueConstraint(name = "uk_person_document", columnNames = "document"),
    @UniqueConstraint(name = "uk_person_email", columnNames = "email")
})
public class Person {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 14) private String document;
    @Column(nullable = false, length = 80) private String name;
    @Column(nullable = false, length = 100) private String surname;
    @Column(nullable = false, length = 150) private String email;

    protected Person() {}
    public Person(String document, String name, String surname, String email) {
        this.document = document; this.name = name; this.surname = surname; this.email = email;
    }
    public Long getId() { return id; }
    public String getDocument() { return document; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
}
