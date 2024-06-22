package com.i27academy.k8s

class K8s {
    def jenkins
    K8s(jenkins) {
        this.jenkins = jenkins
    }

        // Method to authenticate to kubernetes clusters
    def auth_login(gke_cluster_name, gke_zone, gke_project){
        jenkins.sh """
        echo "Entering into Kuberentes Authentication/Login Method"
        gcloud compute instances list
        gcloud container clusters get-credentials $gke_cluster_name --zone $gke_zone --project $gke_project
        echo "********************** Get nodes in the Cluster **********************"
        kubectl get nodes
        """
    }
    // Kubernetes deployment
    def k8sdeploy(fileName, namespace , docker_image){
        jenkins.sh """
        echo "Executing K8S Deploy Method"
        sed -i "s|DIT|${docker_image}|g" ./.cicd/$fileName
        kubectl apply -f ./.cicd/$fileName -n $namespace
        """
    }

    // Helm Deployment
    def k8sHelmChartDeploy() {
        jenkins.sh """
        echo "************************ Executing Helm Groovy Method ************************"
        helm version
        """
    }
}    