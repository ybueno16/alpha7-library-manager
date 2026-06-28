# Alpha7 Library Manager

![Cobertura](.github/badges/jacoco.svg)
![Java](https://img.shields.io/badge/Java-8-blue)
![Swing](https://img.shields.io/badge/UI-Swing%20%2B%20FlatLaf%20Dark-informational)

Sistema de gerenciamento de acervo para pequenas bibliotecas. Desenvolvido como projeto de avaliação para a vaga de Java Pleno na Alpha7 Software.

---

## Funcionalidades

- **Cadastro completo de livros** — título, ISBN, autores, editora, data de publicação, idioma e número de páginas
- **Listagem com busca** — pesquisa por título, autor, ISBN, editora ou idioma (case-insensitive)
- **Criação e edição** via formulário com validação de campos obrigatórios e formato de ISBN
- **Exclusão** com confirmação de diálogo
- **Busca automática por ISBN** — consulta a API pública da OpenLibrary e preenche o formulário; executa em background com `SwingWorker` para não bloquear a UI
- **Importação em lote via CSV** — upsert por ISBN: atualiza livros existentes e insere novos; processa cada linha em transação independente com relatório de erros por linha
- **Livros semelhantes** — associação many-to-many entre livros do acervo

---

## Tecnologias e bibliotecas

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 8 |
| UI | Swing + [FlatLaf](https://www.formdev.com/flatlaf/) (tema escuro) |
| Persistência | Hibernate 5.6 / JPA 2.2 |
| Banco de dados | PostgreSQL 15 + Flyway (migrations versionadas) |
| Connection pool | HikariCP 4 |
| Cache L2 de entidades | Ehcache 3 / JCache (JSR-107) — configurado em `ehcache.xml` |
| Cache de consultas ISBN | Ehcache 3 / JCache (JSR-107) — configurado em `ehcache-isbn-cache.xml` |
| HTTP client | OkHttp 4 (timeout de 5 s) |
| JSON | Jackson Databind |
| CSV | Apache Commons CSV |
| Build | Gradle 8 + Shadow Plugin (fat JAR) |
| Testes | JUnit 5 + Mockito + MockWebServer + embedded-postgres |
| Cobertura | JaCoCo |
| CI/CD | GitHub Actions |

---

## Arquitetura

O projeto adota uma arquitetura em camadas inspirada em Clean Architecture / Hexagonal, com separação estrita de responsabilidades. As anotações JPA ficam exclusivamente na camada de infraestrutura; o domínio é composto por POJOs puros.

### Estrutura de pacotes

O código é organizado **por camada**, não por feature. Cada pacote representa um nível de abstração com regras de dependência estritas: camadas internas (domain) não conhecem camadas externas (infra, presentation).

```
br.com.yuri.alpha7/
├── domain/                   # Núcleo da aplicação — zero dependências de framework
│   ├── livro/
│   │   ├── model/            # Entidade Livro (POJO)
│   │   ├── repository/       # Interface LivroRepository
│   │   └── vo/               # Value Object ISBN
│   ├── autor/
│   │   ├── model/            # Entidade Autor
│   │   └── repository/       # Interface AutorRepository
│   ├── editora/
│   │   ├── model/            # Entidade Editora
│   │   └── repository/       # Interface EditoraRepository
│   └── exception/            # Hierarquia de exceções de domínio
│
├── application/              # Casos de uso — orquestram domínio sem saber de infra ou UI
│   ├── livro/                # BookCrudUseCase, BookSearchUseCase
│   ├── editora/              # EditoraUseCase
│   ├── isbn/                 # IsbnLookupUseCase, porta OpenLibraryClient
│   └── importacao/           # ImportUseCase, ImportResult
│
├── infra/                    # Implementações — Hibernate, HTTP, cache
│   ├── persistence/
│   │   ├── livro/            # LivroEntity, LivroMapper, LivroRepositoryImpl
│   │   ├── autor/            # AutorEntity, AutorMapper, AutorRepositoryImpl
│   │   ├── editora/          # EditoraEntity, EditoraMapper, EditoraRepositoryImpl
│   │   └── converter/        # IsbnConverter (JPA AttributeConverter)
│   └── client/openlibrary/   # OpenLibraryClientImpl, CachingOpenLibraryClient
│
├── presentation/             # Camada de UI — Swing + padrão MVP
│   └── livro/
│       ├── presenter/        # LivroListPresenter, LivroFormPresenter
│       └── view/             # Interfaces LivroListView, LivroFormView + implementações Swing
│
└── config/                   # Wiring manual: InfrastructureConfig, RepositoryConfig, UseCaseConfig
```

### DDD Estratégico

O projeto aplica conceitos de **Domain-Driven Design estratégico**: o código é modelado na linguagem ubíqua do domínio (Livro, Autor, Editora, ISBN), as regras de negócio pertencem ao domínio e à camada de aplicação, e a infraestrutura é detalhe de implementação que pode ser substituído sem tocar no domínio.

Não é DDD tático completo — não há Aggregates explícitos, Domain Events ou padrões como Specification. A escolha foi deliberada: para o escopo da aplicação, Value Object (`ISBN`), Repository e Use Cases são suficientes para expressar o domínio com clareza sem adicionar complexidade desnecessária.

### MVP com Dumb View (Passive View)

A camada de apresentação usa o padrão **MVP** com **Dumb View**: a view não contém nenhuma lógica de negócio nem de apresentação — ela é "burra" por design.

```
┌─────────────────────┐        ┌──────────────────────┐
│  LivroListPresenter │◄──────►│  LivroListView       │
│  (toda a lógica)    │        │  (interface Swing)   │
└────────┬────────────┘        └──────────────────────┘
         │ delega para                  ▲
         ▼                             │ implementa
┌────────────────────┐        ┌──────────────────────┐
│  Use Cases /       │        │  LivroListPanel      │
│  Repositories      │        │  (JPanel concreto)   │
└────────────────────┘        └──────────────────────┘
```

A view expõe apenas:
- **Getters** de campos do formulário (`getTitulo()`, `getIsbn()`, etc.) — retornam `String` crua
- **Setters** para exibir dados (`showLivros(List)`, `setLivro(Livro)`)
- **Callbacks** registrados pelo presenter (`onSave(Runnable)`, `onDelete(Runnable)`)
- **Diálogos** de sistema (`showErrorMessage(String)`, `confirm(String)`)

O presenter orquestra: lê os valores da view, aplica validação e regras de negócio, chama os use cases e comanda a view a exibir o resultado. A view não toma nenhuma decisão. Isso permite testar 100% da lógica de tela com Mockito sem instanciar um único componente Swing.

### Separação domain / JPA

As classes de domínio (`Livro`, `Autor`, `Editora`) são POJOs sem nenhuma anotação JPA ou dependência de framework. As entidades JPA (`LivroEntity`, `AutorEntity`, `EditoraEntity`) ficam em `infra/persistence` e são convertidas para/de domínio pelos mappers (`LivroMapper`, `AutorMapper`, `EditoraMapper`). A conversão ocorre sempre dentro da transação ativa, enquanto a sessão Hibernate está aberta, para evitar `LazyInitializationException` em proxies lazy.

### Padrões de projeto aplicados

| Padrão | Onde |
|---|---|
| MVP + Passive View | `presentation/livro/presenter` + interfaces em `presentation/livro/view` |
| Repository | Interfaces em `domain/*/repository`, implementações em `infra/persistence` |
| Use Case | `application/livro`, `application/editora`, `application/isbn`, `application/importacao` |
| Decorator | `CachingOpenLibraryClient` adiciona cache ao `OpenLibraryClientImpl` |
| Unit of Work | `HibernateUnitOfWork` — propaga `EntityManager` via `ThreadLocal` para repositórios na mesma thread |
| Value Object | `ISBN` — imutável, com validação do dígito verificador (ISBN-10 e ISBN-13) |
| Mapper | `LivroMapper`, `AutorMapper`, `EditoraMapper` — convertem entre domínio e entidade JPA |
| Double-checked locking | `HibernateUtil` e `CacheManagerProvider` — inicialização thread-safe de singletons |

---

## Pré-requisitos

- Java 8
- Docker e Docker Compose (recomendado para subir o banco)

---

## Como executar

### 1. Subir o banco de dados

```bash
docker-compose up -d
```

O container sobe um PostgreSQL 15 com as seguintes credenciais (que também são os valores padrão da aplicação):

| Propriedade | Valor |
|---|---|
| Host / porta | `localhost:5432` |
| Database | `library` |
| Usuário | `library_user` |
| Senha | `library_pass` |

Para usar um banco diferente, passe as credenciais como system properties na hora de executar o JAR (veja abaixo).

### 2. Gerar o fat JAR

```bash
./gradlew shadowJar
```

O artefato é gerado em `build/libs/alpha7-library-manager-1.0-SNAPSHOT.jar`.

### 3. Executar a aplicação

Com as credenciais padrão (Docker):

```bash
java -jar build/libs/alpha7-library-manager-1.0-SNAPSHOT.jar
```

Com banco customizado:

```bash
java -Ddb.url=jdbc:postgresql://localhost:5432/meu_banco \
     -Ddb.user=meu_usuario \
     -Ddb.password=minha_senha \
     -jar build/libs/alpha7-library-manager-1.0-SNAPSHOT.jar
```

Na primeira execução, o Flyway aplica automaticamente as migrations e cria todas as tabelas.

### 4. Executar os testes

```bash
./gradlew test
```

Os testes de repositório sobem um PostgreSQL embedded (sem necessidade de Docker). O relatório de cobertura é gerado em `build/reports/jacoco/test/html/index.html`.

---

## Importação via CSV

O arquivo deve ter cabeçalho com as colunas abaixo. Apenas `titulo` e `isbn` são obrigatórios; os demais campos são opcionais.

```
titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas
Clean Code,9780132350884,"Robert C. Martin",Prentice Hall,2008-08-01,en,431
The Pragmatic Programmer,9780135957059,"David Thomas,Andrew Hunt",Addison-Wesley,2019-09-23,en,352
```

| Campo | Formato | Observação |
|---|---|---|
| `titulo` | texto | obrigatório |
| `isbn` | ISBN-10 ou ISBN-13 | obrigatório; hífens são ignorados |
| `autores` | nomes separados por vírgula | pode estar entre aspas duplas |
| `editora` | texto | opcional |
| `dataPublicacao` | `yyyy-MM-dd` | opcional |
| `idioma` | código de idioma (ex: `en`, `pt`) | opcional |
| `numeroPaginas` | inteiro | opcional |

Livros com ISBN já cadastrado são **atualizados** com os dados do arquivo. Erros em linhas individuais são registrados no resultado da importação sem interromper o processamento das demais.

---

## Esquema do banco de dados

As migrations Flyway criam o seguinte esquema:

```
editora          (id, nome)
autor            (id, nome, data_nascimento, data_falecimento, bio)
livro            (id, titulo, isbn, data_publicacao, numero_paginas, idioma, editora_id → editora)
livro_autor      (livro_id → livro, autor_id → autor)          -- N:N livro ↔ autor
livro_semelhante (livro_id → livro, semelhante_id → livro)     -- N:N livro ↔ livro
```

Índices criados nas migrations: `LOWER(titulo)`, `LOWER(idioma)`, `editora_id` em `livro`; `autor_id` em `livro_autor`; `nome` em `editora` e `autor`.

---

## CI/CD

| Workflow | Trigger | O que faz |
|---|---|---|
| `ci.yml` | Push / PR em `master` | Compila, executa testes, gera relatório JaCoCo e atualiza o badge de cobertura |
| `release.yml` | Tag `v*` ou disparo manual | Gera o fat JAR e cria uma GitHub Release com o artefato anexado |

Para criar uma release:

```bash
git tag v1.0.0
git push origin v1.0.0
```
