#!/bin/bash
cd /home/user/my-app

# Stop existing process
if pgrep -f pos-0.0.1-SNAPSHOT.jar > /dev/null; then
  pkill -f pos-0.0.1-SNAPSHOT.jar
  echo "Stopped existing application process"
  sleep 5
fi

# Create env file with database credentials
cat > .env << EOF
DB_URL=DBURL_PLACEHOLDER
DB_USERNAME=DBUSER_PLACEHOLDER
DB_PASSWORD=DBPASS_PLACEHOLDER
EOF

# Start application with nohup
echo "Starting application with nohup..."
nohup java -jar pos-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=DBURL_PLACEHOLDER \
  --spring.datasource.username=DBUSER_PLACEHOLDER \
  --spring.datasource.password=DBPASS_PLACEHOLDER \
  > app.log 2>&1 &

PID=$!
echo $PID > app.pid
echo "Application started with PID: $PID"