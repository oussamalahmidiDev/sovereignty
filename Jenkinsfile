pipeline {
    agent any

    environment {
        BACKEND_IMAGE = 'sovereignty-backend'
        FRONTEND_IMAGE = 'sovereignty-frontend'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Debug Java/Maven') {
            steps {
                dir('sovereignty-backend') {
                    sh '''
                      echo "PATH=$PATH"
                      echo "JAVA_HOME=$JAVA_HOME"

                      which java
                      java -version

                      which mvn
                      mvn -version
                    '''
                }
            }
        }

        stage('Backend - Test & Build') {
            steps {
                dir('sovereignty-backend') {
                    sh 'mvn clean verify'
                }
            }
        }

        stage('Frontend - Install & Build') {
            steps {
                dir('sovereignty-frontend') {
                    sh 'npm ci'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -f Dockerfile-ci -t $BACKEND_IMAGE:latest ./sovereignty-backend'
                sh 'docker build -t $FRONTEND_IMAGE:latest ./sovereignty-frontend'
            }
        }

        stage('Deploy Local K8s') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                  helm upgrade --install sovereignty ./helm/sovereignty \
                    --set backend.image=$BACKEND_IMAGE:latest \
                    --set frontend.image=$FRONTEND_IMAGE:latest
                '''
            }
        }
    }
}