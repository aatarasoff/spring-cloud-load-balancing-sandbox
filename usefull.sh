# Deploy and delete test app
http POST :8080/v2/apps?force=true < cool-app.json
http DELETE :8080/v2/apps/cool-app?force=true

# Change env variable
http -v --form POST host:port/env TEST_APP_DELAY=300

# Some load on services
http :9090/feign && echo "GET http://localhost:9090/feign" | vegeta attack -rate=30 -duration=300s | tee results.bin | vegeta report