# Spring Cloud Client Load Balancing Sandbox

## Guide
1. Run docker-compose with following command: `docker-compose up -d`
2. Check marathon at `localhost:8080`
3. Build **cool-app**: `./gradlew dockerBuild`
4. Deploy **cool-app**: `http POST :8080/v2/apps?force=true < cool-app.json`
5. Make changes in `/etc/hosts` file:
```
127.0.0.1       mesos-slave.zone1
127.0.0.1       mesos-slave.zone2
```
6. Launch sandbox app: `./gradlew :load-balancing-sandbox:bootRun`
7. Install vegeta with brew or another way
8. Make some load with command: `http :9090/feign && echo "GET http://localhost:9090/feign" | vegeta attack -rate=30 -duration=20s | tee results.bin | vegeta report`
9. Feel free to scale app as a standard marathon feature
10. Make instance slower or faster with passing env variable: `http -v --form POST host:port/env TEST_APP_DELAY=<delay in ms>`
11. Change Ribbon configuration as you want
