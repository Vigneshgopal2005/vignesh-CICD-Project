pipeline {
    agent any

    environment {
        SONAR_URL = "http://56.155.80.17:9000"
        DOCKER_IMAGE = "vigneshgopal2005/ultimate-cicd:${BUILD_NUMBER}"
        GIT_REPO_NAME = "vignesh-CICD-Project"
        GIT_USER_NAME = "Vigneshgopal2005"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Checkout Started"
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh '''
                java -version
                mvn -version

                rm -rf target
                mvn clean package
                '''
            }
        }

        stage('Static Code Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')]) {
                    sh '''
                    mvn sonar:sonar \
                    -Dsonar.login=$SONAR_AUTH_TOKEN \
                    -Dsonar.host.url=${SONAR_URL}
                    '''
                }
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                script {

                    sh '''
                    docker build -t ${DOCKER_IMAGE} .
                    '''

                    docker.withRegistry(
                    'https://index.docker.io/v1/',
                    'docker-cred'
                    ) {

                    sh '''
                    docker push ${DOCKER_IMAGE}
                    '''
                    }
                }
            }
        }

        stage('Update Deployment File') {
            steps {

                withCredentials([string(credentialsId: 'github', variable: 'GITHUB_TOKEN')]) {

                    sh '''
                    git config user.email "vigneshgopal2005@gmail.com"
                    git config user.name "Vigneshgopal2005"

                    sed -i "s/replaceImageTag/${BUILD_NUMBER}/g" deployment.yaml

                    git add deployment.yaml

                    git commit -m "Update image ${BUILD_NUMBER}"

                    git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }

        stage('Deploy to OpenShift') {

            steps {

                sh '''
                oc login --token=sha256~ZmufvrqxSleA7NKEIxqxQgtIk8DSuoYz_vGUmV4X-Mo --server=https://api.rm1.0a51.p1.openshiftapps.com:6443

                oc project vigneshgopal2005-dev

                oc apply -f deployment.yaml

                oc rollout restart deployment/springboot-app

                oc get pods
                '''
            }
        }

    }

    post {

        success {

            echo "CI/CD Pipeline Completed Successfully"
        }

        failure {

            echo "Pipeline Failed"
        }
    }
}
