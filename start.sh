#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Log level colors
INFO_COLOR='\033[0;32m'      # Green for INFO
WARN_COLOR='\033[1;33m'      # Bold Yellow for WARN  
ERROR_COLOR='\033[1;31m'     # Bold Red for ERROR

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
    docker-compose down
    
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

# Show combined logs with prefixes and color-coded log levels
tail -f $ANGULAR_LOG $SPRINGBOOT_LOG | while read line; do
    if [[ $line == "==> "* ]]; then
        # Skip file separator lines from tail
        continue
    elif [[ $line == *"ng serve"* ]] || [[ $line == *"Angular"* ]] || [[ $line == *"webpack"* ]] || [[ $line == *"Local:"* ]] || [[ $line == *"External:"* ]]; then
        echo -e "${GREEN}[ANGULAR]${NC} $line"
    elif [[ $line == *"Spring"* ]] || [[ $line == *"Tomcat"* ]] || [[ $line == *"INFO"* ]] || [[ $line == *"WARN"* ]] || [[ $line == *"ERROR"* ]]; then
        # Color-code only the log level keywords in Spring Boot logs
        colored_line="$line"
        if [[ $line == *"ERROR"* ]]; then
            # Replace ERROR with colored version (handles various formats like "ERROR ", "ERROR[", etc.)
            colored_line=$(printf '%s\n' "$line" | sed 's/\bERROR\b/\x1b[1;31mERROR\x1b[0m/g')
        elif [[ $line == *"WARN"* ]]; then
            colored_line=$(printf '%s\n' "$line" | sed 's/\bWARN\b/\x1b[1;33mWARN\x1b[0m/g')
        elif [[ $line == *"INFO"* ]]; then
            colored_line=$(printf '%s\n' "$line" | sed 's/\bINFO\b/\x1b[0;32mINFO\x1b[0m/g')
        fi
        echo -e "${BLUE}[SPRING-BOOT]${NC} $colored_line"
    else
        echo "$line"
    fi
done
