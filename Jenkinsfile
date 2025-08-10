// Declarative pipeline syntax
pipeline {
    // Run this pipeline on any available Jenkins agent
    agent any

    // Define environment variables that will be available to all stages
    environment {
        // Set the base URL for your API tests.
        // It's best practice to manage this via Jenkins credentials or configuration
        // for different environments (e.g., staging, production).
        BASE_URL = 'https://fakerestapi.azurewebsites.net/api/v1'

        // Define a name for your Docker image.
        // Using the Jenkins BUILD_NUMBER makes each image tag unique.
        DOCKER_IMAGE_NAME = "seva-makhinia/avengatest:${env.BUILD_NUMBER}"

        // Define the directory where reports will be stored on the Jenkins agent.
        REPORTS_DIR = 'reports'
    }

    stages {
        // Stage 1: Build the Docker image from your Dockerfile
        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image: ${DOCKER_IMAGE_NAME}"
                    // The '.' indicates that the Dockerfile is in the root of the workspace.
                    docker.build(DOCKER_IMAGE_NAME, '.')
                }
            }
        }

        // Stage 2: Run the tests inside the new Docker container
        stage('Run API Tests') {
            steps {
                // Ensure the reports directory exists and is clean before the run.
                sh "mkdir -p ${REPORTS_DIR}"

                echo "Running tests with BASE_URL: ${BASE_URL}"
                // Execute the docker run command.
                // --rm: Automatically removes the container when it exits.
                // -e: Sets the environment variable inside the container.
                // -v: Mounts the host's report directory to the container's report directory.
                sh "/Users/loki/.rd/bin/docker run --rm -e BASE_URL=${env.BASE_URL} -v ${pwd()}/${REPORTS_DIR}:/app/reports ${DOCKER_IMAGE_NAME}"
            }
        }

        // Stage 3: Publish the HTML report for easy viewing
        stage('Publish Test Report') {
            steps {
                echo "Archiving and publishing HTML report from '${REPORTS_DIR}'..."
                // Use the HTML Publisher plugin to display the report.
                // Jenkins will look inside the 'test-reports' directory for any .html file.
                publishHTML(target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: REPORTS_DIR,
                        reportFiles: '**.html', // This wildcard finds your uniquely named report file
                        reportName: 'API Test Report'
                ])
            }
        }
    }

    // Post-build actions: This block runs after all stages are complete
    post {
        always {
            // This is a good place for cleanup tasks.
            echo "Cleaning up the workspace and Docker image."

            // Clean the Jenkins workspace to save disk space.
            cleanWs()

            // Optional: Remove the Docker image created during the build to save space.
            // Be cautious with this in production if you need to inspect images later.
            sh "docker rmi ${DOCKER_IMAGE_NAME} || true"
        }
    }
}
