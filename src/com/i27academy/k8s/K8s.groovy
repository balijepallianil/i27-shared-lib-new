package com.i27academy.k8s

class K8s {
    def jenkins
    K8s(jenkins) {
        this.jenkins = jenkins
    }
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