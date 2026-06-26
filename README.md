# Alpha7 Library Manager

![CI](https://github.com/ybueno16/Alpha7LibraryManager/actions/workflows/ci.yml/badge.svg)
![Cobertura](.github/badges/jacoco.svg)
![Java](https://img.shields.io/badge/Java-8-blue)
![Swing](https://img.shields.io/badge/UI-Swing%20%2B%20FlatLaf-informational)

Sistema de gerenciamento de acervo para pequenas bibliotecas. Desenvolvido como projeto de avaliação para a vaga de Java Pleno na Alpha7 Software.

---

## Funcionalidades

- **Cadastro completo de livros** — título, ISBN, autores, editora, data de publicação, idioma, páginas e livros semelhantes
- **Listagem com busca** — pesquisa por qualquer campo (título, autor, ISBN, editora, idioma)
- **Criação e edição** via formulário com validação de campos obrigatórios
- **Exclusão** com confirmação
- **Busca por ISBN via OpenLibrary** — preenche o formulário automaticamente consultando a API pública; executa em background com SwingWorker para não travar a UI
- **Importação de CSV** — importa em lote com upsert: atualiza livros existentes e insere novos
- **Cache de consultas** — resultados da OpenLibrary são cacheados com Ehcache/JCache para evitar requisições repetidas

---

## Tecnologias e bibliotecas

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 8 |
| UI | Swing + [FlatLaf](https://www.formdev.com/flatlaf/) |
| Persistência | Hibernate 5.6 / JPA 2.2 |
| Banco de dados | PostgreSQL + Flyway (migrations) |
| Connection pool | HikariCP |
| Cache L2 | Ehcache 3 / JCache (JSR-107) |
| HTTP client | OkHttp 4 |
| JSON | Jackson Databind |
| CSV | Apache Commons CSV |
| Build | Gradle 8 + Shadow Plugin (fat JAR) |
| Testes | JUnit 5 + Mockito + MockWebServer + embedded-postgres |
| Cobertura | JaCoCo (≥ 91% de branches) |
| CI/CD | GitHub Actions |

---

## Arquitetura

O projeto segue uma arquitetura em camadas com separação clara de responsabilidades:

```
presentation/   → MVP (Model-View-Presenter) com Swing
application/    → Casos de uso (regras de negócio)
domain/         → Entidades, repositórios (interfaces) e value objects
infra/          → Implementações: JPA, HTTP client, cache, migrations
config/         → Montagem manual de dependências (sem framework DI)
```

### Padrões de projeto aplicados

- **MVP** — separação entre View (interface Swing), Presenter (lógica) e Model (domínio)
- **Repository** — abstração de acesso a dados com interfaces no domínio
- **Use Case** — cada operação de negócio isolada em sua própria classe
- **Decorator** — `CachingOpenLibraryClient` decora `OpenLibraryClientImpl` adicionando cache
- **Unit of Work** — controle transacional encapsulado em `HibernateUnitOfWork`
- **Value Object** — `ISBN` com validação do dígito verificador (ISBN-10 e ISBN-13)
- **Double-checked locking** — inicialização thread-safe de `HibernateUtil` e `CacheManagerProvider`

---

## Pré-requisitos

- Java 11+ (para executar; código-fonte compatível com Java 8)
- PostgreSQL 12+
- Docker (opcional, para subir o banco via `docker-compose.yml`)

---

## Como executar

### 1. Subir o banco de dados

```bash
docker-compose up -d
```

Ou configure um PostgreSQL local e ajuste as variáveis em `gradle.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/alpha7library
db.username=postgres
db.password=postgres
```

### 2. Executar a aplicação

```bash
./gradlew run
```

Ou, após gerar o fat JAR:

```bash
./gradlew shadowJar
java -jar build/libs/alpha7-library-manager-1.0-SNAPSHOT.jar
```

### 3. Executar os testes

```bash
./gradlew test
```

O relatório de cobertura é gerado em `build/reports/jacoco/test/html/index.html`.

---

## Importação via CSV

O arquivo deve ter cabeçalho e seguir o formato:

```
titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas
Clean Code,9780132350884,"Robert C. Martin",Prentice Hall,2008-01-01,en,431
```

- **Autores múltiplos**: separados por vírgula dentro de aspas
- **dataPublicacao**: formato `yyyy-MM-dd`
- **Livros já existentes** (mesmo ISBN) são atualizados com os dados do arquivo

---

## CI/CD

| Workflow | Trigger | O que faz |
|---|---|---|
| `ci.yml` | Push/PR em `master` | Executa testes, gera relatório de cobertura e atualiza o badge |
| `release.yml` | Tag `v*` ou disparo manual | Gera o fat JAR e cria uma GitHub Release |

Para criar uma release:

```bash
git tag v1.0.0
git push origin v1.0.0
```
