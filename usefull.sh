# Deploy and delete test app
http POST :8080/v2/apps?force=true < cool-app.json
http DELETE :8080/v2/apps/cool-app?force=true

# Change env variable
http host:port?delay?ms=300

# Some load on services
http :10080/callme && echo "GET http://localhost:10080/callme" | vegeta attack -duration=600s -rate=120 | vegeta report