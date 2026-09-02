$env:DB_URL = "jdbc:postgresql://localhost:5432/pool_practice"
$env:DB_USERNAME = "pool_app"
$env:DB_PASSWORD = "TODO-use-a-local-practice-password"
$env:JAVA_TOOL_OPTIONS = "-Duser.timezone=Asia/Ho_Chi_Minh"

mvn exec:java

POSTGRES_DB=pool_practice
POSTGRES_USER=pool_app
POSTGRES_PASSWORD=TODO-use-a-local-practice-password
POSTGRES_PORT=5432
