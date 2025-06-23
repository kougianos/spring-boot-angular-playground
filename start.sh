#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Starting Spring Boot Angular Playground...${NC}"
echo "=========================================="

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}Stopping services...${NC}"
    
    # Kill Angular dev server
    if [ ! -z "$ANGULAR_PID" ]; then
        kill $ANGULAR_PID 2>/dev/null
        echo "Angular dev server stopped"
    fi
    
    # Kill Spring Boot app
    if [ ! -z "$SPRINGBOOT_PID" ]; then
        kill $SPRINGBOOT_PID 2>/dev/null
        echo "Spring Boot application stopped"
    fi
    
    # Stop Docker services
    echo "Stopping Docker services..."
    docker compose down
    
    echo -e "${GREEN}All services stopped.${NC}"
    exit 0
}

# Set trap for cleanup (temp files are handled by another trap)
trap cleanup SIGINT SIGTERM

# Check if Docker is running and start infrastructure
echo -e "${BLUE}Starting Docker services...${NC}"
if ! docker compose up -d; then
    echo -e "${RED}Error: Failed to start Docker services. Please make sure Docker is running.${NC}"
    exit 1
fi

echo -e "${GREEN}Docker services started successfully!${NC}"
echo "Waiting 5 seconds for services to initialize..."
sleep 5

# Create temporary log files
ANGULAR_LOG=$(mktemp)
SPRINGBOOT_LOG=$(mktemp)

# Add cleanup of temp files to the cleanup function
trap 'rm -f "$ANGULAR_LOG" "$SPRINGBOOT_LOG"; cleanup' EXIT SIGINT SIGTERM

# Start Angular development server
echo -e "\n${BLUE}Starting Angular development server...${NC}"
cd angular
npm start > $ANGULAR_LOG 2>&1 &
ANGULAR_PID=$!
cd ..

# Start Spring Boot application
echo -e "${BLUE}Starting Spring Boot application...${NC}"
cd spring-boot
mvn spring-boot:run > $SPRINGBOOT_LOG 2>&1 &
SPRINGBOOT_PID=$!
cd ..

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Both services are starting...${NC}"
echo -e "${YELLOW}Angular will be available at: http://localhost:4200${NC}"
echo -e "${YELLOW}Spring Boot will be available at: http://localhost:8080${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}Showing combined logs (Press Ctrl+C to stop all services):${NC}"
echo ""

# Show combined logs with prefixes
tail -f $ANGULAR_LOG $SPRINGBOOT_LOG | while read line; do
    if [[ $line == "==> logs/angular.log <==" ]]; then
        echo -e "${GREEN}[ANGULAR]${NC}"
    elif [[ $line == "==> logs/springboot.log <==" ]]; then
        echo -e "${BLUE}[SPRING-BOOT]${NC}"
    elif [[ $line != "==> "* ]]; then
        if [[ $line == *"ng serve"* ]] || [[ $line == *"Angular"* ]] || [[ $line == *"webpack"* ]]; then
            echo -e "${GREEN}[ANGULAR]${NC} $line"
        elif [[ $line == *"Spring"* ]] || [[ $line == *"Tomcat"* ]] || [[ $line == *"INFO"* ]]; then
            echo -e "${BLUE}[SPRING-BOOT]${NC} $line"
        else
            echo "$line"
        fi
    fi
done
