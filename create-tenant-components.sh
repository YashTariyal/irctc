#!/bin/bash
# Helper script to create tenant components for a service
SERVICE=$1
PACKAGE=$(echo $SERVICE | sed 's/irctc-//' | sed 's/-service//')
echo "Creating tenant components for $SERVICE with package com.irctc.$PACKAGE"
