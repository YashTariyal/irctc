#!/bin/bash

# Deployment Script
# Automates the deployment process

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

DEPLOYMENT_TYPE="${1:-docker}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Payment Service Deployment${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Validate environment
echo -e "${YELLOW}Step 1: Validating environment...${NC}"
if ! ./scripts/validate-env.sh; then
    echo -e "${RED}❌ Environment validation failed${NC}"
    exit 1
fi
echo ""

# Build application
echo -e "${YELLOW}Step 2: Building application...${NC}"
cd irctc-payment-service
if ./mvnw clean package -DskipTests; then
    echo -e "${GREEN}✅ Build successful${NC}"
else
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
fi
cd ..
echo ""

# Deploy based on type
case $DEPLOYMENT_TYPE in
    docker)
        echo -e "${YELLOW}Step 3: Deploying with Docker Compose...${NC}"
        
        # Check if docker-compose is available
        if ! command -v docker-compose &> /dev/null; then
            echo -e "${RED}❌ docker-compose not found${NC}"
            exit 1
        fi
        
        # Build and start
        docker-compose -f docker-compose.prod.yml build
        docker-compose -f docker-compose.prod.yml up -d
        
        echo -e "${GREEN}✅ Docker deployment started${NC}"
        echo ""
        echo -e "${YELLOW}Waiting for service to be healthy...${NC}"
        sleep 10
        
        # Check health
        if curl -f http://localhost:8083/actuator/health > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Service is healthy${NC}"
        else
            echo -e "${RED}❌ Service health check failed${NC}"
            echo -e "${YELLOW}Check logs: docker-compose -f docker-compose.prod.yml logs${NC}"
            exit 1
        fi
        ;;
    
    kubernetes)
        echo -e "${YELLOW}Step 3: Deploying with Kubernetes...${NC}"
        
        # Check if kubectl is available
        if ! command -v kubectl &> /dev/null; then
            echo -e "${RED}❌ kubectl not found${NC}"
            exit 1
        fi
        
        # Apply configurations
        kubectl apply -f k8s/namespace.yaml
        kubectl apply -f k8s/secrets.yaml
        kubectl apply -f k8s/configmap.yaml
        kubectl apply -f k8s/deployment.yaml
        kubectl apply -f k8s/service.yaml
        
        echo -e "${GREEN}✅ Kubernetes deployment started${NC}"
        echo ""
        echo -e "${YELLOW}Waiting for pods to be ready...${NC}"
        kubectl wait --for=condition=ready pod -l app=payment-service --timeout=300s
        
        echo -e "${GREEN}✅ Pods are ready${NC}"
        ;;
    
    *)
        echo -e "${RED}❌ Unknown deployment type: $DEPLOYMENT_TYPE${NC}"
        echo "Usage: ./scripts/deploy.sh [docker|kubernetes]"
        exit 1
        ;;
esac

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Display service information
echo -e "${YELLOW}Service Information:${NC}"
echo "Health Check: http://localhost:8083/actuator/health"
echo "API Base: http://localhost:8083/api/payments"
echo "Analytics: http://localhost:8083/api/payments/analytics/overview"
echo ""

# Run post-deployment tests
read -p "Do you want to run post-deployment tests? (y/n): " RUN_TESTS

if [ "$RUN_TESTS" = "y" ] || [ "$RUN_TESTS" = "Y" ]; then
    echo ""
    echo -e "${YELLOW}Running post-deployment tests...${NC}"
    ./scripts/test-deployment.sh
fi

echo ""
echo -e "${GREEN}✅ Deployment successful!${NC}"

