echo You should build jar before running this script. See README for details

java -jar target/client-cursor-0.0.1-SNAPSHOT.jar ^
 --spring.datasource.url=jdbc:h2:mem:testdb ^
 --spring.datasource.username=sa ^
 --spring.datasource.password=password ^
 --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
