#!/bin/bash
set -euo pipefail

# Configuration
PGADMIN_CONTAINER="pgadmin4"
PGADMIN_PORT="5050"  # Accessible at http://localhost:5050
PGADMIN_EMAIL="admin@admin.com"
PGADMIN_PASSWORD="admin"
NETWORK_NAME="dbmate-net"

# Start pgAdmin 4 container
echo "Starting pgAdmin 4 container..."
docker run --rm -d \
  --name "$PGADMIN_CONTAINER" \
  --network "$NETWORK_NAME" \
  -e PGADMIN_DEFAULT_EMAIL="$PGADMIN_EMAIL" \
  -e PGADMIN_DEFAULT_PASSWORD="$PGADMIN_PASSWORD" \
  -p "$PGADMIN_PORT:80" \
  dpage/pgadmin4

echo "✅ pgAdmin 4 is running at: http://localhost:$PGADMIN_PORT"
echo "🔐 Login with:"
echo "   Email:    $PGADMIN_EMAIL"
echo "   Password: $PGADMIN_PASSWORD"

echo ""
echo "💡 Once logged in, create a new connection with:"
echo "   Hostname: pg-dev-db"
echo "   Port:     5432"
echo "   Username: devuser"
echo "   Password: devpass"
echo "   Database: devdb"
