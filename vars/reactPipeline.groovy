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
        choice (name: 'dockerPush',
            choices: 'no\nyes',
            description: "Docker Build and push to registry"
        )
    }
    tools{
        jdk 'JDK-17'
    }
    environment {
        APPLICATION_NAME = "${pipelineParams.appName}"
        DOCKER_HUB = "docker.io/i27anilb3"
        DOCKER_CREDS = credentials('docker_creds')
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
                k8s.mageBuildFrontEnd("${env.APPLICAION_NAME}")
            }

        }
    }
} 

