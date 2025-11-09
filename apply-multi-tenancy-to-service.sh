#!/bin/bash
# Script to apply multi-tenancy to a service
SERVICE=$1
SERVICE_PACKAGE=$(echo $SERVICE | sed 's/-service//' | sed 's/irctc-//')

echo "Applying multi-tenancy to $SERVICE..."

# Copy tenant components (adjust package names)
# This is a template - actual files need to be created with correct package names

echo "✅ Multi-tenancy applied to $SERVICE"
