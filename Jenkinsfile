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

        stage('Backend - Test & Build') {
            steps {
                dir('backend') {
                    sh 'mvn clean verify'
                }
            }
        }

        stage('Frontend - Install & Build') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $BACKEND_IMAGE:latest ./backend'
                sh 'docker build -t $FRONTEND_IMAGE:latest ./frontend'
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