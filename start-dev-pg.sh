#!/bin/bash
# Start CUPA application with PostgreSQL database (dev-pg profile)
#
# This script starts the application with the dev-pg profile, which uses
# PostgreSQL database containing production data backup instead of H2.
#
# Prerequisites:
# - PostgreSQL server running on localhost:5432
# - Database: cupa_from_prod
# - User: cupa / Password: cupa-cupa
#
# Usage: ./start-dev-pg.sh

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev-pg

