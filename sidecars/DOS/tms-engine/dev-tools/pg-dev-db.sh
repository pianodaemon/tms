#!/bin/bash
set -euo pipefail

# Configuration
CONTAINER_NAME="pg-dev-db"
DB_USER="devuser"
DB_PASS="devpass"
DB_NAME="devdb"
DB_PORT="5433"  # Use a non-standard port to avoid conflicts
NETWORK_NAME="dbmate-net"
POSTGRES_IMAGE="postgres:17"
DBMATE_IMAGE="amacneil/dbmate"

SCRIPT_DIR=$(cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)

# Migration directory
MIGRATIONS_DIR="$SCRIPT_DIR/../shared-test-resources/src/test/resources/db/migration"
ORIGINAL_FILE="$MIGRATIONS_DIR/V1__cargo.sql"

# Ensure migration file exists
if [[ ! -f "$ORIGINAL_FILE" ]]; then
  echo "ERROR: Migration file $ORIGINAL_FILE not found."
  exit 1
fi

# Generate dbmate-style migration filename based on file mtime
timestamp=$(date -r "$ORIGINAL_FILE" +"%Y%m%d%H%M%S")
LINKED_FILE="$MIGRATIONS_DIR/${timestamp}_V1__cargo.sql"

cleanup() {
  if [[ -f "$LINKED_FILE" ]]; then
    echo "Cleaning up migration link: $LINKED_FILE"
    rm -f "$LINKED_FILE"
  fi
}

trap cleanup EXIT

# Create hard link if not already present
if [[ ! -f "$LINKED_FILE" ]]; then
  echo "Creating dbmate-compatible migration link: $LINKED_FILE"
  ln "$ORIGINAL_FILE" "$LINKED_FILE"
else
  echo "Migration link already exists: $LINKED_FILE"
fi

# Create Docker network if needed
if ! docker network ls | grep -q "$NETWORK_NAME"; then
  echo "Creating Docker network: $NETWORK_NAME"
  docker network create "$NETWORK_NAME"
fi

# Start PostgreSQL container
echo "Starting PostgreSQL container: $CONTAINER_NAME"
docker run --rm -d \
  --name "$CONTAINER_NAME" \
  --network "$NETWORK_NAME" \
  -e POSTGRES_USER="$DB_USER" \
  -e POSTGRES_PASSWORD="$DB_PASS" \
  -e POSTGRES_DB="$DB_NAME" \
  -p "$DB_PORT":5432 \
  "$POSTGRES_IMAGE"

# Wait for PostgreSQL to become available
echo "Waiting for database to be ready..."
until docker exec "$CONTAINER_NAME" pg_isready -U "$DB_USER" >/dev/null 2>&1; do
  sleep 1
done

# Run dbmate from container
echo "Running dbmate migrations..."
docker run --rm \
  --network "$NETWORK_NAME" \
  -v "$MIGRATIONS_DIR:/db/migrations" \
  -e DATABASE_URL="postgres://$DB_USER:$DB_PASS@$CONTAINER_NAME:5432/$DB_NAME?sslmode=disable" \
  "$DBMATE_IMAGE" \
  --migrations-dir=/db/migrations \
  up

echo "✅ Migration complete. PostgreSQL is running at localhost:$DB_PORT"
