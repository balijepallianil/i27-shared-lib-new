import com.i27academy.builds.Docker
import com.i27academy.k8s.K8s

def call(Map pipelineParams) {
    Docker docker = new Docker(this)
    K8s k8s = new K8s(this)
pipeline {
    agent {
        label 'k8s-slave'
    }
    parameters {
        choice (name: 'buildOnly',
               choices: 'no\nyes',
               description: "Build the Application Only"
        )
        choice (name: 'dockerPush',
               choices: 'no\nyes',
               description: "Docker Build and push to registry"
        )
    }    

    tools{
        maven 'Maven-3.8.8'
        jdk 'JDK-17'
    }
    environment {
        APPLICATION_NAME = "${pipelineParams.appName}"
        //APPLICATION_NAME = "eureka"
        POM_VERSION = readMavenPom().getVersion()
        POM_PACKAGING = readMavenPom().getPackaging()
        DOCKER_HUB = "docker.io/i27anilb3"
        DOCKER_CREDS = credentials('docker_creds')
        GKE_DEV_CLUSTER_NAME = "cart-cluster"
        GKE_DEV_ZONE = "us-west1-a"
        GKE_DEV_PROJECT = "glass-approach-423807-a5"
        K8S_DEV_FILE = "k8s_dev.yaml"
        K8S_TST_FILE = "k8s_tst.yaml"
        DEV_NAMESPACE = "cart-dev-ns"
    }
    stages{
        stage ('Authentication') {
                steps {
                    echo "Executing in GCP project"
                    script{
                        k8s.auth_login("${env.GKE_DEV_CLUSTER_NAME}", "${env.GKE_DEV_ZONE}", "${env.GKE_DEV_PROJECT}")
                    }
            }
        }
       stage ('MavenBuild') {
            when {
                anyOf {
                    expression {
                        params.buildOnly == 'yes'
                    }
                }
            }

            
            steps {
                script {
                    //buildApp().call()
                    println docker.add(5,6)
                    docker.buildApp("${env.APPLICATION_NAME}")
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
                    dockerBuildandPush().call()
                }

            }
        }
    }
}
}

def buildApp() {
    return {
        echo "Bulding ${env.APPLICATION_NAME} application"
        sh 'mvn clean package -DskipTests=true'

    }
}

def dockerBuildandPush() {
    return {
        echo "Starting Docker build stage"
        sh "cp ${WORKSPACE}/target/i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} ./.cicd/"
        echo "**************************** Building Docker Image ****************************"
        sh "docker build --force-rm --no-cache --build-arg JAR_SOURCE=i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} -t ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT} ./.cicd"
        echo "********Docker login******"
        sh "docker login -u ${DOCKER_CREDS_USR} -p ${DOCKER_CREDS_PSW}"
        echo "********Docker Push******"
        sh "docker push ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
    }
}



