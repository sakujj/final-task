./gradlew :prepareForDocker
docker compose -f ./docker-config/docker-compose.yaml down
docker compose -f ./docker-config/docker-compose.yaml build --no-cache
docker compose -f ./docker-config/docker-compose.yaml up -d
