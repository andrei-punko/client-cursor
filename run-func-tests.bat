echo "Run functional tests. Check reports at ./build/spock-reports/index.html"

cd func-tests
gradlew clean build
