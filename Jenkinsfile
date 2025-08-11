pipeline {
    agent any

    parameters {
        string(
                name: 'BASE_URL_PARAM',
                defaultValue: 'https://fakerestapi.azurewebsites.net/api/v1',
                description: 'The base URL for the FakeRESTApi tests'
        )
    }

    environment {
        FakeRESTApi = "${params.BASE_URL_PARAM}"
        DOCKER_IMAGE_NAME = "seva-makhinia/avengatest:${env.BUILD_NUMBER}"
        REPORTS_DIR = 'reports'
    }

    stages {
        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image: ${DOCKER_IMAGE_NAME}"
                    docker.build(DOCKER_IMAGE_NAME, '.')
                }
            }
        }

        stage('Run API Tests') {
            steps {
                sh "mkdir -p ${REPORTS_DIR}"

                echo "Running tests with BASE_URL: ${BASE_URL}"
                sh "docker run --rm -e BASE_URL=${env.BASE_URL} -v ${pwd()}/${REPORTS_DIR}:/app/reports ${DOCKER_IMAGE_NAME}"
            }
        }
    }

    post {
        always {
            echo "Archiving and publishing HTML report from '${REPORTS_DIR}'..."
            publishHTML(target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: REPORTS_DIR,
                    reportFiles: '**.html',
                    reportName: 'FakeRESTApi Test Report'
            ])
        }
    }
}
