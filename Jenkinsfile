pipeline {
    agent {
        docker {
            image 'maven:3.8.6-openjdk-11'
            args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checkout completed'
            }
        }

        stage('Build and Test') {
            steps {
                sh 'ls -ltr'
                sh 'mvn clean package'
            }
        }

        stage('Static Code Analysis') {
            environment {
                SONAR_URL='http://15.152.97.7:9000'
            }

            steps {
                withCredentials([string(credentialsId:'sonarqube',variable:'SONAR_AUTH_TOKEN')]) {

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
                DOCKER_IMAGE='vigneshgopal2005/ultimate-cicd:${BUILD_NUMBER}'
            }

            steps {

                script {

                    sh '''
                    docker build -t ${DOCKER_IMAGE} .
                    '''

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

                withCredentials([string(credentialsId:'github',variable:'GITHUB_TOKEN')]) {

                    sh '''
                    git config user.email "vigneshgopal2005@gmail.com"

                    git config user.name "Vigneshgopal2005"

                    sed -i "s/replaceImageTag/${BUILD_NUMBER}/g" cicd-project/deployment.yaml

                    git add cicd-project/deployment.yaml

                    git commit -m "Update image ${BUILD_NUMBER}" || true

                    git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }

        stage('Deploy to OpenShift') {

            steps {

                sh '''
                oc login --token=sha256~TBpU2qcfxujAs0XrExzxPSI_hN24OYfSTg9u_1GDoB8 --server=https://api.rm1.0a51.p1.openshiftapps.com:6443

                oc project vigneshgopal2005-dev

                oc apply -f cicd-project/deployment.yaml
                '''
            }
        }
    }
}
