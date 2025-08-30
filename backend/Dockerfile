FROM openjdk:17-jdk
# JAR 파일이 저장될 작업 디렉토리 설정
WORKDIR /app
# Maven 또는 Gradle 빌드 후 생성된 JAR 파일을 컨테이너 내부 /app 디렉토리에
COPY build/libs/backend-0.0.1-SNAPSHOT.jar app.jar
# 4. 실행 포트 지정
EXPOSE 80
# 5. 컨테이너 실행 시 JAR 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
