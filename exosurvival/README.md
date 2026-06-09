# ExoSurvival — Backend API

REST API para o jogo educacional **ExoSurvival**, onde jogadores configuram planetas usando dados reais de exoplanetas e gerenciam recursos para sobreviver.

## Tecnologias

- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL 16
- Docker + Docker Compose
- Maven

## Arquitetura

```
src/main/java/com/exosurvival/
├── config/          # SecurityConfig, CORS
├── controller/      # AuthController, PlanetController, GameSessionController
├── dto/
│   ├── request/     # RegisterRequest, LoginRequest, PlanetRequest, GameSessionRequest
│   └── response/    # AuthResponse, PlanetResponse, GameSessionResponse, ApiResponse
├── entity/          # User, Planet, GameSession
├── exception/       # GlobalExceptionHandler + exceções customizadas
├── repository/      # UserRepository, PlanetRepository, GameSessionRepository
├── security/        # JwtUtil, JwtAuthenticationFilter
├── service/         # AuthService, PlanetService, GameSessionService, UserDetailsServiceImpl
└── util/            # DifficultyCalculator, DifficultyProfile, ExoplanetBaseline
```

## Como rodar

### Com Docker (recomendado)

```bash
# Na raiz do projeto (onde está o docker-compose.yml)
cp .env.example .env
docker-compose up --build
```

A API estará disponível em `http://localhost:8080`.

### Localmente

Requisitos: Java 17, Maven, PostgreSQL rodando.

```bash
cd backend
mvn spring-boot:run
```

## Endpoints

### Auth — `/api/v1/auth`

| Método | Rota        | Autenticação | Descrição          |
|--------|-------------|--------------|--------------------|
| POST   | `/register` | ❌            | Criar conta        |
| POST   | `/login`    | ❌            | Login + token JWT  |

### Planets — `/api/v1/planets`

| Método | Rota    | Autenticação | Descrição                           |
|--------|---------|--------------|-------------------------------------|
| POST   | `/`     | ✅            | Criar planeta + receber perfil de dificuldade |
| GET    | `/`     | ✅            | Listar planetas do usuário          |
| GET    | `/{id}` | ✅            | Buscar planeta por ID               |
| DELETE | `/{id}` | ✅            | Deletar planeta                     |

### Sessions — `/api/v1/sessions`

| Método | Rota      | Autenticação | Descrição                        |
|--------|-----------|--------------|----------------------------------|
| POST   | `/`       | ✅            | Salvar resultado de uma partida  |
| GET    | `/`       | ✅            | Histórico de partidas do usuário |
| GET    | `/best`   | ✅            | Melhor partida (maior duração)   |

## Lógica de Dificuldade

Ao criar um planeta, o sistema compara cada parâmetro com a média de exoplanetas catalogados (NASA Exoplanet Archive):

| Parâmetro             | Baseline (média) | Desvio padrão |
|-----------------------|------------------|---------------|
| Temperatura (°C)      | 15.0             | ±20.0         |
| Gravidade (m/s²)      | 9.8              | ±4.0          |
| Pressão atm (atm)     | 1.0              | ±0.5          |
| Oxigênio (%)          | 21.0             | ±8.0          |
| Disponibilidade hídrica | 0.7            | ±0.3          |
| Radiação solar        | 1.0              | ±0.4          |

O `DifficultyProfile` retornado contém multiplicadores por recurso (ex: `oxygenDecayMultiplier: 1.8` = oxigênio drena 80% mais rápido que o baseline).

## Níveis de dificuldade

| Label           | Score  |
|-----------------|--------|
| HABITABLE       | < 0.4  |
| CHALLENGING     | < 0.8  |
| HOSTILE         | < 1.3  |
| EXTREME         | < 2.0  |
| UNSURVIVABLE    | ≥ 2.0  |

## Autores

Desenvolvido como projeto acadêmico para a **Global Solution** — Engenharia de Software (FIAP).

Bruno Otavio Silva De Oliveira RM556196

Guilherme Flores Pereira de Almeida RM554948

Luiz Fernando de Aragão Souza RM555561

Bruno Otavio Silva De Oliveira RM556196

Marcello de Freitas Moreira RM557531

Leonardo Gonçalves Novaes RM554807