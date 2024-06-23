import com.i27academy.builds.Docker
import com.i27academy.k8s.K8s
import com.i27academy.k8s.React

def call(Map pipelineParams) {
    Docker docker = new Docker(this)
    K8s k8s = new K8s(this)
    React react = new React(this)
    pipeline {
        agent {
            label 'k8s-slave'
        }
        parameters {
            choice (name: 'dockerPush',
                choices: 'no\nyes',
                description: "Docker Build and push to registry"
            )
            choice (name: 'deployToDev',
                choices: 'no\nyes',
                description: "Deploy app in DEV"
            )
        }
        tools{
            jdk 'JDK-17'
        }
        environment {
            APPLICATION_NAME = "${pipelineParams.appName}"
            DOCKER_HUB = "docker.io/i27anilb3"
            DOCKER_CREDS = credentials('docker_creds')
            DEV_NAMESPACE = "i27-dev-ns"
            FILE_PATH = "${WORKSPACE}/k8s-dev.yaml"
            VALUES_PATH = "${WORKSPACE}/values_dev.yaml"
            DEV_ENV = "dev"
            HELM_PATH = "${WORKSPACE}/i27-shared-lib-new/chart"
        }
        stages {
            stage ('Checkout'){
                steps {
                    println("Checkout: Cloning git repo for i27Shared Library *************")
                    script {
                        k8s.gitclone()
                    }
                }
            }
            stage ('Docker Build and push') {
                when {
                    anyOf {
                        expression {
                            params.dockerPush == 'yes'
                        }
                    }
                } 
                steps {
                    script  {
                        imageBuildFrontEnd().call()
                    }
                }
            }
            stage ('Deploy to Dev') {
                when {
                    anyOf {
                        expression {
                            params.deployToDev == 'yes'
                        }
                    }
                } 
                steps {
                    script  {
                        def docker_image = "${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
                        echo "${docker_image}"
                        imageValidation().call()
                        react.k8sHelmChartDeploy("${env.APPLICATION_NAME}", "${env.DEV_ENV}" , "${env.HELM_PATH}", "${GIT_COMMIT}", "${env.VALUES_PATH}")
                        //react.k8sdeploy("${env.FILE_PATH}", "${env.DEV_NAMESPACE}", docker_image)
                    }
                }
            }
        }
    }       
}
def imageBuildFrontEnd() {
    return{
        echo "**************************** Building Docker Image ****************************"
        sh "docker build --force-rm --no-cache -t ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT} ."
        echo "********Docker login******"
        sh "docker login -u ${DOCKER_CREDS_USR} -p ${DOCKER_CREDS_PSW}"
        echo "********Docker Push******"
        sh "docker push ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
    }
}
def imageValidation() {
    return {
        println ("pulling he docker Image")
        try {
            sh "docker pull ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
        } 
        catch (Exception e) {
            println("OOPS!!!!!, docker image with this tag doesnot exists, So creating the image")
            imageBuildFrontEnd().call()
        }
    }
}