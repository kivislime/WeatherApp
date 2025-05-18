mvn clean package
docker cp target/weatherApp-1.0-SNAPSHOT.war WeatherTomcat:/usr/local/tomcat/webapps/WeatherApp.war
docker restart WeatherTomcat