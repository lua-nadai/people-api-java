package br.com.desafio.peopleapi.controller;

import br.com.desafio.peopleapi.dto.PersonRequest;
import br.com.desafio.peopleapi.model.Person;
import br.com.desafio.peopleapi.repository.PersonRepository;
import br.com.desafio.peopleapi.service.NationalityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping
public class PersonController {
    private final PersonRepository repository;
    private final NationalityService nationalityService;

    public PersonController(PersonRepository repository, NationalityService nationalityService) {
        this.repository = repository; this.nationalityService = nationalityService;
    }

    @PostMapping("/registrarName")
    @ResponseStatus(HttpStatus.CREATED)
    public Person create(@Valid @RequestBody PersonRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (repository.existsByDocument(request.document()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Documento já cadastrado");
        if (repository.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        return repository.save(new Person(request.document(), request.name().trim(),
            request.surname().trim(), email));
    }

    @GetMapping("/list")
    public List<Person> list() { return repository.findAll(); }

    @GetMapping("/list/{id}")
    public Person getById(@PathVariable @Positive(message = "ID deve ser positivo") Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada"));
    }

    @DeleteMapping("/list/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive(message = "ID deve ser positivo") Long id) {
        if (!repository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada");
        repository.deleteById(id);
    }

    @GetMapping("/findNacionalityByPerson/{id}")
    public NationalityService.NationalityResult nationality(@PathVariable @Positive Long id) {
        Person person = getById(id);
        return nationalityService.predict(person.getName());
    }
}
