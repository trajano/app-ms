if "%1"=="" (
  mvn -q -DskipTests=true clean package && docker-compose down --remove-orphans && docker-compose up --build -d
) else if NOT "%2"=="" (
  mvn -q -DskipTests=true -am -pl %1 package && docker-compose rm -fs %2 && docker-compose up --build -d %2
) else (
  mvn -q -DskipTests=true -am -pl %1 package
)