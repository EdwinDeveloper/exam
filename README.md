# Pokedex Manager

A full-stack Pokédex application with a React frontend and a Spring Boot REST API. The backend browses Pokémon from PokeAPI and lets authenticated users manage local Pokémon records.

## Solution Overview

- **Frontend:** React functional components built with Vite.
- **Backend:** Java 25 and Spring Boot 3.5.16 REST API.
- **Architecture:** The backend separates web adapters, application use cases and ports, domain models, and persistence/external-service adapters.
- **Pokémon persistence:** Spring Data JPA, using H2 by default; PostgreSQL can be configured through environment variables.
- **External catalog:** PokeAPI, accessed through the `PokeApiGateway` adapter.
- **Authentication:** Username/password registration and sign-in, followed by a signed HS256 JWT bearer token for protected endpoints.

## Run Locally

Prerequisites: Java 25, Maven, and Node.js with npm.

Start the backend in one terminal:

```sh
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:8080` by default.

Install frontend dependencies and start Vite in another terminal:

```sh
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

The frontend expects the API at `http://localhost:8080`.

## Authentication

Registration and sign-in are public. Local collection and catalog endpoints require the access token returned by sign-in.

1. Register with a username and password.
2. Sign in with those credentials.
3. The API returns an access token valid for one hour.
4. The frontend sends it on protected requests as `Authorization: Bearer <accessToken>`.
5. Sign out to clear the token from frontend memory. The user must sign in again after a page refresh or token expiry.

Example registration request:

```http
POST /api/auth/register
Content-Type: application/json

{"username":"trainer","password":"a-long-password"}
```

Example sign-in request:

```http
POST /api/auth/login
Content-Type: application/json

{"username":"trainer","password":"a-long-password"}
```

Sign-in does not require an existing token; it verifies the credentials and issues one.

### JWT Secret

The backend reads its signing key from `JWT_SECRET`. The configured fallback is for local development only. Set a private secret of at least 32 bytes in deployed environments, and do not commit production secrets.

## API

`POST /api/auth/register` and `POST /api/auth/login` are public. All `/api/pokemon/**` routes require a valid bearer token, including catalog browsing and detail lookup.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/pokemon/catalog?offset=0&limit=24` | Browse the remote catalog. `limit` must be from 1 to 60. Response includes `count`, `next`, `previous`, and `results`. |
| `GET` | `/api/pokemon/catalog/{name}` | Get remote Pokémon details. |
| `GET` | `/api/pokemon/local` | List local Pokémon records. |
| `GET` | `/api/pokemon/local/{id}` | Get a local record. |
| `POST` | `/api/pokemon/sync/{name}` | Copy a remote Pokémon into the local store. |
| `POST` | `/api/pokemon/local` | Create a local record. |
| `PUT` | `/api/pokemon/local/{id}` | Update a local record. |
| `DELETE` | `/api/pokemon/local/{id}` | Delete a local record. |

API errors use a JSON `error` field where available, for example:

```json
{"error":"A local record for ivysaur already exists"}
```

## Data Storage

The default datasource is an in-memory H2 database. Data is lost when the backend process stops. For a persistent PostgreSQL database, configure:

- `DB_URL`: JDBC URL
- `DB_USERNAME`: database username
- `DB_PASSWORD`: database password

The persistence layer currently uses JPA and is not configured for MongoDB. Pokémon access is behind the `PokemonStore` application port, which provides a boundary for adding another adapter.

**Current ownership limitation:** local Pokémon records are not associated with individual users. JWT authentication protects the endpoints, but authenticated users currently share the same local collection.

## Validation

Run backend tests:

```sh
cd backend
mvn test
```

Build the frontend:

```sh
cd frontend
npm run build
```
