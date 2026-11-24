package br.com.desafio.peopleapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:people-test",
    "app.security.username=test",
    "app.security.password=pass"
})
class PeopleApiTest {
    private static final String BASIC = "Basic dGVzdDpwYXNz";

    @Autowired
    WebApplicationContext context;

    @Test
    void validatesAndManagesPeopleWithAuthentication() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilters(new DelegatingFilterProxy("springSecurityFilterChain", context))
            .build();

        mvc.perform(get("/list")).andExpect(status().isUnauthorized());
        mvc.perform(get("/list/abc").header("Authorization", BASIC)).andExpect(status().isBadRequest());
        mvc.perform(get("/list/0").header("Authorization", BASIC)).andExpect(status().isBadRequest());
        mvc.perform(delete("/list/-1").header("Authorization", BASIC)).andExpect(status().isBadRequest());
        mvc.perform(get("/findNacionalityByPerson/0").header("Authorization", BASIC)).andExpect(status().isBadRequest());

        mvc.perform(post("/registrarName").header("Authorization", BASIC).contentType(MediaType.APPLICATION_JSON)
            .content("{}")).andExpect(status().isBadRequest());

        String person = """
            {"document":"12345678901","name":"Nathaniel","surname":"Silva","email":"nathaniel@example.com"}
            """;
        mvc.perform(post("/registrarName").header("Authorization", BASIC).contentType(MediaType.APPLICATION_JSON)
            .content(person)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
        mvc.perform(post("/registrarName").header("Authorization", BASIC).contentType(MediaType.APPLICATION_JSON)
            .content(person.replace("12345678901", "12345678902").replace("nathaniel@", "NATHANIEL@")))
            .andExpect(status().isConflict());
        mvc.perform(get("/list").header("Authorization", BASIC)).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/list/1").header("Authorization", BASIC)).andExpect(jsonPath("$.name").value("Nathaniel"));
        mvc.perform(delete("/list/1").header("Authorization", BASIC)).andExpect(status().isNoContent());
        mvc.perform(get("/list/1").header("Authorization", BASIC)).andExpect(status().isNotFound());
    }
}
