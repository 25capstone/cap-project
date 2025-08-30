pipeline {
    agent any
    tools {
        jdk 'JDK17'                 // Jenkins Global Tool 이름 (네가 등록한 JDK 이름)
    }
    environment {
        DOCKER_IMAGE       = "sns-backend"
        CONTAINER_NAME     = "sns-backend"
        JAR_FILE_NAME      = "app.jar"          // 컨테이너 내부에서 사용할 이름
        PORT               = "80"               // 외부 포트 (외부 80 → 내부 8080 매핑)
        REMOTE_USER        = "ec2-user"
        REMOTE_HOST        = "13.125.245.127"  // ✅ spring-server 퍼블릭 IP로 변경
        REMOTE_DIR         = "/home/ec2-user/deploy"
        SSH_CREDENTIALS_ID = "8999928c6-c147-49c3-975d-7a7f3f52f431"  // ✅ Jenkins에 등록한 SSH 자격증명 ID
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build Jar') {
            steps {
                dir('backend') {
                    sh './gradlew bootJar -x test'
                }
            }
        }

        stage('Prepare Artifact') {
            steps {
                dir('backend') {
                    // build/libs 안의 부트 JAR을 app.jar로 복사
                    sh 'cp build/libs/*.jar ${JAR_FILE_NAME}'
                }
            }
        }

        stage('Copy to Remote') {
            steps {
                sshagent (credentials: [env.SSH_CREDENTIALS_ID]) {
                    sh """
                      ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "mkdir -p ${REMOTE_DIR}"
                      scp -o StrictHostKeyChecking=no backend/${JAR_FILE_NAME} backend/Dockerfile ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/
                    """
                }
            }
        }

        stage('Remote Docker Build & Run') {
            steps {
                sshagent (credentials: [env.SSH_CREDENTIALS_ID]) {
                    sh """
                      ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} << 'ENDSSH'
                      cd ${REMOTE_DIR} || exit 1
                      docker rm -f ${CONTAINER_NAME} || true
                      docker build -t ${DOCKER_IMAGE} .
                      docker run -d --name ${CONTAINER_NAME} -p ${PORT}:8080 ${DOCKER_IMAGE}
                      ENDSSH
                    """
                }
            }
        }
    }

    post {
        always { cleanWs() }
    }
}
