FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .

# Create application.yaml with environment variable placeholders
RUN mkdir -p src/main/resources && \
    echo 'spring:' > src/main/resources/application.yaml && \
    echo '  application:' >> src/main/resources/application.yaml && \
    echo '    api-prefix: ${API_PREFIX:/api/v1}' >> src/main/resources/application.yaml && \
    echo '    name: spring-boot-rest-api' >> src/main/resources/application.yaml && \
    echo '  jpa:' >> src/main/resources/application.yaml && \
    echo '    hibernate:' >> src/main/resources/application.yaml && \
    echo '      ddl-auto: create-drop' >> src/main/resources/application.yaml && \
    echo '    show-sql: true' >> src/main/resources/application.yaml && \
    echo 'server:' >> src/main/resources/application.yaml && \
    echo '  port: ${PORT:8080}' >> src/main/resources/application.yaml && \
    echo '  address: 0.0.0.0' >> src/main/resources/application.yaml && \
    echo 'pulse:' >> src/main/resources/application.yaml && \
    echo '  server:' >> src/main/resources/application.yaml && \
    echo '    url: ${PULSE_SERVER_URL:http://localhost:8080}' >> src/main/resources/application.yaml && \
    echo 'frontend:' >> src/main/resources/application.yaml && \
    echo '  url: ${FRONTEND_URL:http://localhost:3000}' >> src/main/resources/application.yaml && \
    echo 'payment:' >> src/main/resources/application.yaml && \
    echo '  vnPay:' >> src/main/resources/application.yaml && \
    echo '    url: ${PAY_URL:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}' >> src/main/resources/application.yaml && \
    echo '    tmnCode: ${TMN_CODE:0PTMND1X}' >> src/main/resources/application.yaml && \
    echo '    secretKey: ${SECRET_KEY:8R0MTL44745GJQAV9BA8VR8HMCIPTCMB}' >> src/main/resources/application.yaml && \
    echo '    returnUrl: ${RETURN_URL:https://vnpay-api.onrender.com/api/v1/payment/vn-pay-callback}' >> src/main/resources/application.yaml && \
    echo '    version: ${VERSION:2.1.0}' >> src/main/resources/application.yaml && \
    echo '    command: ${COMMAND:pay}' >> src/main/resources/application.yaml && \
    echo '    orderType: ${ORDER_TYPE:other}' >> src/main/resources/application.yaml && \
    echo '    mockTime: ${MOCK_TIME:false}' >> src/main/resources/application.yaml && \
    echo '    mockDateTime: ${MOCK_DATETIME:20251230115000}' >> src/main/resources/application.yaml

RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]
