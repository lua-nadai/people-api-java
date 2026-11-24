# Quipux — Desafio Java

Aplicação REST em Java 17 + Spring Boot, autenticação HTTP Basic e interface responsiva com Bootstrap 5.3.9 (código aberto, licença MIT).

## Requisitos
- Java 17+
- Maven 3.9+
- Acesso à internet para consultar o Nationalize e carregar a folha de estilos do Bootstrap via jsDelivr.

## Executar
Na pasta do projeto:

```bash
mvn spring-boot:run
```

A aplicação inicia em `http://localhost:8080`.

Credenciais padrão para desenvolvimento:
- Usuário: `admin`
- Senha: `admin123`

Altere as credenciais por variáveis de ambiente antes de usar fora de um ambiente local:

**Linux/macOS**
```bash
API_USER=usuario API_PASSWORD='senha' mvn spring-boot:run
```

**PowerShell**
```powershell
$env:API_USER="usuario"
$env:API_PASSWORD="senha"
mvn spring-boot:run
```

Abra `http://localhost:8080` para acessar a interface.

## Endpoints

Todos os endpoints da API exigem autenticação HTTP Basic.

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/registrarName` | Cadastra uma pessoa |
| GET | `/list` | Lista todas as pessoas |
| GET | `/list/{id}` | Busca por ID |
| DELETE | `/list/{id}` | Exclui por ID |
| GET | `/findNacionalityByPerson/{id}` | Consulta a nacionalidade provável pelo nome da pessoa |

## Validações e regras
- Documento obrigatório, com 11 dígitos (CPF) ou 14 dígitos (CNPJ).
- Nome e sobrenome obrigatórios.
- E-mail obrigatório e validado pelo formato de e-mail.
- ID deve ser positivo.
- Documento e e-mail são únicos.
- Cadastro duplicado retorna HTTP 409; pessoa inexistente retorna HTTP 404; dados inválidos retornam HTTP 400.

## Testes

Execute `mvn test` para verificar autenticação, cadastro, validação de IDs, conflito por e-mail, listagem, busca, exclusão e a integração simulada com Nationalize. Os testes usam H2 em memória e uma resposta HTTP simulada, sem precisar de chave externa.
