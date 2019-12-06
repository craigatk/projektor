docker run -e "DB_URL=jdbc:postgresql://192.168.0.103:5433/projektordb" -e "PORT=8080" -p 80:8080 myapp:v1.0.2
