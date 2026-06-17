pipeline {
    agent {
        label 'onprem-agent'
    }

    options {
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        ansiColor('xterm')
    }

    environment {
        REGISTRY_URL     = 'registry.gitlab.com'
        IMAGE_NAME       = '5kstration/payment-api-event-server'
        IMAGE_TAG        = "${BUILD_NUMBER}"
        IMAGE_FULL_NAME  = "${REGISTRY_URL}/${IMAGE_NAME}:${IMAGE_TAG}"
        IMAGE_LATEST     = "${REGISTRY_URL}/${IMAGE_NAME}:latest"

        REGISTRY_CRED_ID = 'gitlab-registry-credentials'
        KUBECONFIG_ID    = 'k8s-kubeconfig'
        K8S_NAMESPACE    = 'service'
        K8S_DEPLOYMENT   = 'payment-api-event-server'
    }

    stages {
        stage('Checkout SCM') {
            steps {
                echo '[Checkout] GitLab repository checkout'
                checkout scm
            }
        }

        stage('Backend Test & Build') {
            steps {
                echo '[Build] Run tests and create Spring Boot JAR'
                sh '''
                    if [ -f "./gradlew" ] && [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
                      chmod +x ./gradlew
                      ./gradlew clean test bootJar --no-daemon
                    else
                      gradle clean test bootJar --no-daemon
                    fi
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '[SonarQube] Run static code analysis'
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    withSonarQubeEnv('SonarQube-Server') {
                        sh './gradlew sonar'
                    }
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }

        stage('Docker Login') {
            steps {
                echo '[Docker] Login to container registry'
                withCredentials([usernamePassword(credentialsId: "${REGISTRY_CRED_ID}", usernameVariable: 'REGISTRY_USERNAME', passwordVariable: 'REGISTRY_PASSWORD')]) {
                    sh '''
                        echo "${REGISTRY_PASSWORD}" | docker login "${REGISTRY_URL}" \
                          --username "${REGISTRY_USERNAME}" \
                          --password-stdin
                    '''
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                echo '[Docker] Build and push Payment API Event Server image'
                sh '''
                    docker build -t "${IMAGE_FULL_NAME}" .
                    docker tag "${IMAGE_FULL_NAME}" "${IMAGE_LATEST}"
                    docker push "${IMAGE_FULL_NAME}"
                    docker push "${IMAGE_LATEST}"
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo '[Deploy] Apply Kubernetes manifests and roll out Payment API Event Server'
                withCredentials([file(credentialsId: "${KUBECONFIG_ID}", variable: 'KUBECONFIG')]) {
                    sh '''
                        set -e
                        kubectl --kubeconfig=${KUBECONFIG} version --request-timeout=10s
                        kubectl --kubeconfig=${KUBECONFIG} apply -f k8s/deployment.yaml
                        kubectl --kubeconfig=${KUBECONFIG} apply -f k8s/service.yaml
                        kubectl --kubeconfig=${KUBECONFIG} apply -f k8s/istio.yaml
                        kubectl --kubeconfig=${KUBECONFIG} set image rollout/${K8S_DEPLOYMENT} \
                          ${K8S_DEPLOYMENT}=${IMAGE_FULL_NAME} \
                          -n ${K8S_NAMESPACE}
curl -sLO https://github.com/argoproj/argo-rollouts/releases/latest/download/kubectl-argo-rollouts-linux-amd64
                        chmod +x ./kubectl-argo-rollouts-linux-amd64
                        ./kubectl-argo-rollouts-linux-amd64 --kubeconfig=${KUBECONFIG} -n $K8S_NAMESPACE status ${K8S_DEPLOYMENT} --timeout=360s
                          -n ${K8S_NAMESPACE} \
                          --timeout=360s
                    '''
                }
            }
        }

        stage('Health Check') {
            steps {
                echo '[Health] Check Kubernetes deployment and pod state'
                withCredentials([file(credentialsId: "${KUBECONFIG_ID}", variable: 'KUBECONFIG')]) {
                    sh '''
                        set -e
                        kubectl --kubeconfig=${KUBECONFIG} get rollout ${K8S_DEPLOYMENT} -n ${K8S_NAMESPACE}
                        kubectl --kubeconfig=${KUBECONFIG} get pods -n ${K8S_NAMESPACE} -l app=${K8S_DEPLOYMENT}
                    '''
                }
            }
        }
    }

    post {
        always {
            echo '[Cleanup] Remove local Docker image cache'
            sh '''
                docker rmi "${IMAGE_FULL_NAME}" || true
                docker rmi "${IMAGE_LATEST}" || true
            '''
            sh "docker builder prune -af || true"
            sh "docker system prune -af --volumes || true"
        }
        success {
            echo '[Success] Payment API Event Server Kubernetes deployment completed'
            sh """
            curl -H "Content-Type: application/json" \\
                 -d '{"content": "??**Î∞∞Ìè¨ ?±Í≥µ**: ${env.JOB_NAME} [ÎπåÎìú #${env.BUILD_NUMBER}] Î∞∞Ìè¨Í∞Ä Ïπ¥ÎÇòÎ¶?Canary) Î∞©Ïãù?ºÎ°ú ?àÏ†Ñ?òÍ≤å ?ÑÎ£å?òÏóà?µÎãà?? ??"}' \\
                 ""
            """
        }
        failure {
            echo '[Failure] Payment API Event Server pipeline failed. Fetching pod logs for debugging...'
            sh """
            curl -H "Content-Type: application/json" \\
                 -d '{"content": "?ö® **Î∞∞Ìè¨ ?§Ìå®**: ${env.JOB_NAME} [ÎπåÎìú #${env.BUILD_NUMBER}] ?êÎü¨ Î∞úÏÉù! ?åÏù¥?ÑÎùº??Î°úÍ∑∏Î•??ïÏù∏?¥Ï£º?∏Ïöî."}' \\
                 ""
            """
            withCredentials([file(credentialsId: "${KUBECONFIG_ID}", variable: 'KUBECONFIG')]) {
                sh '''
                    echo "=== Pod Status ==="
                    kubectl --kubeconfig=${KUBECONFIG} get pods -n ${K8S_NAMESPACE} -l app=${K8S_DEPLOYMENT} || true
                    echo "=== Pod Describe ==="
                    kubectl --kubeconfig=${KUBECONFIG} describe pods -n ${K8S_NAMESPACE} -l app=${K8S_DEPLOYMENT} || true
                    echo "=== Application Logs ==="
                    kubectl --kubeconfig=${KUBECONFIG} logs -n ${K8S_NAMESPACE} -l app=${K8S_DEPLOYMENT} --all-containers=true --tail=100 || true
                '''
            }
        }
    }
}
