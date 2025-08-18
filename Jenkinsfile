// Define a script-level variable to hold the image name
def IMAGE_NAME

pipeline {
    agent any

    tools {
        jdk 'GraalVM_JAVA_21'
    }

    parameters {
        booleanParam(name: 'NATIVE_BUILD', defaultValue: false, description: 'Check this to build a native executable instead of a JVM application.')
        booleanParam(name: 'IS_RELEASE_BUILD', defaultValue: false, description: 'Check this to perform a release build. This will update versions and create a git tag.')
    }

    environment {
        APP_NAME = 'sentinel'
        CLIENT_NAME = 'creditco'
        GIT_CREDENTIALS_ID = 'ssh_key'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Release') {
            // This stage only runs for release builds.
            when { expression { return params.IS_RELEASE_BUILD } }
            steps {
                script {
                    echo "Preparing for release..."

                    // Ensure we are on a clean main branch for releases
                    sh "git checkout main"
                    // Fetch the latest state from the remote repository
                    sh "git fetch origin"
                    // Hard reset the local main branch to match the remote, discarding any local changes
                    sh "git reset --hard origin/main"
                    // Clean the workspace of any untracked files from previous builds
                    sh "git clean -fdx"

                    def pom = readMavenPom file: 'pom.xml'
                    if (!pom.version.endsWith('-SNAPSHOT')) {
                        error("ERROR: The project version in pom.xml is not a snapshot version. Cannot perform release.")
                    }

                    // Calculate the release version by removing "-SNAPSHOT"
                    def releaseVersion = pom.version.replace('-SNAPSHOT', '')

                    // Use the Maven versions plugin to set the new version in pom.xml
                    sh "./mvnw versions:set -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"

                    // Configure git user for the commit
                    sh "git config --global user.email 'jenkins@example.com'"
                    sh "git config --global user.name 'Jenkins CI'"

                    // Commit the version change locally, bypassing pre-commit hooks.
                    sh "git commit --no-verify -am 'Release version ${releaseVersion}'"
                }
            }
        }

        stage('Build and Push Image') {
            steps {
                script {
                    // Read the version from the (potentially modified) pom.xml
                    def pom = readMavenPom file: 'pom.xml'
                    def appVersion = pom.version

                    def baseImageName = "docker.bpm-id.com/${env.CLIENT_NAME}/${env.APP_NAME}"
                    if (params.NATIVE_BUILD) {
                        IMAGE_NAME = "${baseImageName}:${appVersion}-native"
                    } else {
                        IMAGE_NAME = "${baseImageName}:${appVersion}"
                    }

                    echo "Building Image: ${IMAGE_NAME}"

                    docker.withRegistry('https://docker.bpm-id.com', "docker-admin-creds") {
                        if (params.NATIVE_BUILD) {
                            echo "Starting Native Build..."
                            sh './mvnw clean verify'
                            sh './mvnw -P prod,native native:compile -DskipTests'
                            echo "Building Native Docker image from src/main/docker/app-native..."
                            docker.build(IMAGE_NAME, '-f src/main/docker/app-native .')
                        } else {
                            echo "Starting Standard JVM Build with Buildpacks..."
                            sh "./mvnw -Pprod spring-boot:build-image -Dspring-boot.build-image.imageName=${IMAGE_NAME}"
                        }

                        echo "Pushing image: ${IMAGE_NAME}"
                        docker.image(IMAGE_NAME).push()
                    }
                }
            }
        }

        stage('Finalize Release') {
            // This stage also only runs for release builds.
            when { expression { return params.IS_RELEASE_BUILD } }
            steps {
                script {
                    echo "Finalizing release..."
                    def pom = readMavenPom file: 'pom.xml'
                    def releaseVersion = pom.version

                    // Tag the release in Git
                    sh "git tag -a v${releaseVersion} -m 'Version ${releaseVersion}'"

                    // Calculate the next development (snapshot) version
                    def (major, minor, patch) = releaseVersion.tokenize('.')
                    def nextPatch = patch.toInteger() + 1
                    def nextSnapshotVersion = "${major}.${minor}.${nextPatch}-SNAPSHOT"

                    // Set the next snapshot version in pom.xml
                    sh "./mvnw versions:set -DnewVersion=${nextSnapshotVersion} -DgenerateBackupPoms=false"

                    // Commit the new snapshot version locally, bypassing pre-commit hooks.
                    sh "git commit --no-verify -am 'Prepare for next development iteration'"

                    // Push the commits and the new tag using SSH key authentication
                    withCredentials([sshUserPrivateKey(credentialsId: env.GIT_CREDENTIALS_ID, keyFileVariable: 'GIT_SSH_KEY')]) {
                        sh 'git push origin main'
                        sh 'git push origin --tags'
                    }
                }
            }
        }
    }
}
