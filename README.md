# URL Shortener

A production-oriented URL shortener built with Spring Boot, PostgreSQL, Redis, Docker Compose, React, and Nginx.

The interesting part of this project is the ID generation strategy. Instead of generating random short codes and checking for collisions, each backend instance reserves a batch of numeric IDs from Redis, converts them to Base62, and persists the mapping in PostgreSQL. That keeps the app stateless, reduces Redis traffic, and scales cleanly across multiple backend containers.

https://github.com/user-attachments/assets/65cb7819-fa39-44ab-8bb2-83117786844b

## Architecture

```text
Client
  |
  v
Nginx
  |
  +--> Spring Boot instance 1
  +--> Spring Boot instance 2
  +--> Spring Boot instance 3
            |
            +--> Redis (batched counter allocation)
            |
            +--> PostgreSQL (persistent URL mappings)
```

## Tech Stack

- `Spring Boot` for the backend API
- `PostgreSQL` as the source of truth for URL mappings
- `Redis` for distributed counter allocation
- `Nginx` as the reverse proxy and load balancer
- `Docker Compose` for local multi-service orchestration
- `React + Vite` for the frontend

## How It Works

### 1. URL shortening

When a client submits a long URL:

1. The backend asks the local counter allocator for the next numeric ID.
2. If the instance still has IDs left in its local batch, it serves one from memory.
3. If the batch is exhausted, the instance reserves the next block from Redis using `INCRBY`.
4. The numeric ID is converted to a Base62 short code.
5. The `shortCode -> longUrl` mapping is stored in PostgreSQL.

### 2. Redirects

When a client hits a short code:

1. The backend looks up the code in PostgreSQL.
2. If found, it returns an HTTP redirect to the original long URL.

## Counter Batching

This project uses a batched counter allocation strategy to avoid hitting Redis on every shorten request.

Why this matters:

- Fewer network calls to Redis
- Lower latency on the hot path
- Better horizontal scalability
- No random-code collision handling in the normal path

Each backend instance keeps a local range:

- `current`: next ID to hand out
- `max`: upper bound of the current reserved batch

When the instance runs out of IDs, it reserves the next batch from Redis.

## API

### Create short URL

`POST /api/v1/shorten`

Request:

```json
{
  "longUrl": "https://example.com/some/very/long/url"
}
```

Response:

```json
{
  "shortCode": "1zA"
}
```

### Redirect

`GET /api/v1/{shortCode}`

Returns:

- `302 Found` with `Location` header when the code exists
- `404 Not Found` when the code does not exist

## Running Locally

### Prerequisites

- `Docker`
- `Docker Compose`

### Start the stack

```bash
docker compose up --build --scale backend=3
```

Services:

- Nginx: `http://localhost:8090`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`

## Load Testing

You can bombard the backend with concurrent shorten requests using `xargs`:

```bash
seq 1 10000 | xargs -I{} -P 50 curl -s -o /dev/null -w "%{http_code}\n" \
  -X POST http://localhost:8090/api/v1/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl":"https://example.com/very/very/long/url/{}"}'
```

## Future Improvements

- Health checks and readiness probes
- Metrics for batch exhaustion and redirect latency
- Database migrations with Flyway or Liquibase
- Redis replication / managed Redis
- Caching hot redirects
