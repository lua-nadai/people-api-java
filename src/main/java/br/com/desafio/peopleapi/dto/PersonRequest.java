package br.com.desafio.peopleapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PersonRequest(
    @NotBlank(message = "Documento é obrigatório")
    @Pattern(regexp = "\\d{11}|\\d{14}", message = "Documento deve conter 11 ou 14 dígitos numéricos")
    String document,
    @NotBlank(message = "Nome é obrigatório") @Size(max = 80) String name,
    @NotBlank(message = "Sobrenome é obrigatório") @Size(max = 100) String surname,
    @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") @Size(max = 150) String email
) {}
