pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                echo 'Checkout completed'
            }
        }

        stage('Build and Test') {
            steps {

                sh 'java -version'
                sh 'mvn -version'

                sh '''
                rm -rf target
                mvn clean package
                '''
            }
        }

        stage('Static Code Analysis') {

            environment {
                SONAR_URL='http://15.152.97.7:9000'
            }

            steps {

                withCredentials([string(
                credentialsId:'sonarqube',
                variable:'SONAR_AUTH_TOKEN')]) {

                    sh '''
                    mvn sonar:sonar \
                    -Dsonar.login=$SONAR_AUTH_TOKEN \
                    -Dsonar.host.url=${SONAR_URL}
                    '''
                }
            }
        }

        stage('Build and Push Docker Image') {

            environment {
                DOCKER_IMAGE="vigneshgopal2005/ultimate-cicd:${BUILD_NUMBER}"
            }

            steps {

                script {

                    sh """
                    docker build -t ${DOCKER_IMAGE} .
                    """

                    def dockerImage=docker.image("${DOCKER_IMAGE}")

                    docker.withRegistry(
                    'https://index.docker.io/v1/',
                    'docker-cred') {

                        dockerImage.push()
                    }
                }
            }
        }

        stage('Update Deployment File') {

            environment {
                GIT_REPO_NAME='vignesh-CICD-Project'
                GIT_USER_NAME='Vigneshgopal2005'
            }

            steps {

                withCredentials([string(
                credentialsId:'github',
                variable:'GITHUB_TOKEN')]) {

                    sh '''
                    git config user.email "vigneshgopal2005@gmail.com"

                    git config user.name "Vigneshgopal2005"

                    sed -i "s/replaceImageTag/${BUILD_NUMBER}/g" deployment.yaml

                    git add deployment.yaml

                    git commit -m "Update image ${BUILD_NUMBER}" || true

                    git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }

        stage('Deploy to OpenShift') {

            steps {

                sh '''
                oc login --token=sha256~GefWWuLx032U7zzslJFvWHu4Ke8rJALY6KxojGXX_RI \
                --server=https://api.rm1.0a51.p1.openshiftapps.com:6443

                oc project vigneshgopal2005-dev

                oc apply -f deployment.yaml

                oc get pods
                '''
            }
        }
    }
}
