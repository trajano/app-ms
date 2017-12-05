docker-compose build && docker-compose -f docker-compose.yml -f docker-stack.yml config | docker stack deploy -c - appms
