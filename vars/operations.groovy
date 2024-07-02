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
        string(name: 'NAMESPACE_NAME', description: "Enter the name of the namespace, you want to create")
        string(name: 'LABEL_NAME', description: "Enter the name of the label, you want to create")
    }
    environment {
        APPLICATION_NAME = "${pipelineParams.appName}"
        //APPLICATION_NAME = "eureka"
        DOCKER_HUB = "docker.io/i27anilb3"
        DOCKER_CREDS = credentials('docker_creds')
        GKE_DEV_CLUSTER_NAME = "cart-cluster"
        GKE_DEV_ZONE = "us-west1-a"
        GKE_DEV_PROJECT = "glass-approach-423807-a5"
        K8S_DEV_FILE = "k8s_dev.yaml"                 
        K8S_TST_FILE = "k8s_tst.yaml"
        K8S_STAGE_FILE = "k8s_stage.yaml"
        K8S_PROD_FILE = "k8s_prod.yaml"
        DEV_NAMESPACE = "i27-dev-ns"
        TST_NAMESPACE = "i27-test-ns"
        STAGE_NAMESPACE = "i27-stage-ns"
        PROD_NAMESPACE = "i27-prod-ns"
    }
    stages {
        stage ('Authentication') {
            steps {
              echo "Executing in GCP project"
                script{
                    k8s.auth_login("${env.GKE_DEV_CLUSTER_NAME}", "${env.GKE_DEV_ZONE}", "${env.GKE_DEV_PROJECT}")
                }
            }
        }
        stage ('Create K8S Namespace'){
            steps {
                script {
                    k8s.namespace_creation("${params.NAMESPACE_NAME}", "${LABEL_NAME}")
                }
            }
        }
    }
}
}
       