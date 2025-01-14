# learning-neo4j
 
# docker
`docker pull neo4j:5.26.0-enterprise`

```docker
docker run -d \
--name neo4j \
-p 7474:7474 \
-p 7687:7687 \
-e NEO4J_AUTH=neo4j/Admin@123 \
-e NEO4J_ACCEPT_LICENSE_AGREEMENT=yes \
neo4j:5.26.0-enterprise
```

```docker-compose
version: '3'
services:
  neo4j:
    image: neo4j:5.26.0-enterprise
    container_name: neo4j
    ports:
      - "7474:7474"
      - "7687:7687"
    environment:
      - NEO4J_AUTH=neo4j/Admin@123
      - NEO4J_ACCEPT_LICENSE_AGREEMENT=yes
```
