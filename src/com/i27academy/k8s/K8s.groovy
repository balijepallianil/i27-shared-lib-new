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
    def k8sHelmChartDeploy(appName, env, helmChartPath, imageTag){
        jenkins.sh """
        echo "Verifying if Chart Exists"
        if helm list | grep -q "${appName}-${env}-chart"; then 
          echo "This Charts Exists!!!!!!!"
          echo "Upgrading the Chart !!!!!"
          helm upgrade ${appName}-${env}-chart -f ./.cicd/k8s/values_${env}.yaml --set image.tag=${imageTag} ${helmChartPath}
        else
          echo "Chart doesnot exists !!!!!!"
          echo "Installing the Chart"
          helm install ${appName}-${env}-chart -f ./.cicd/k8s/values_${env}.yaml --set image.tag=${imageTag} ${helmChartPath}
        fi
        """
    }

    def gitclone() {
        jenkins.sh """
        echo "*******clone the Shared Library repo*****"
        git clone -b main https://github.com/balijepallianil/i27-shared-lib-new.git
        """
    }
    def namespace_creation(namespace_name){
        jenkins.sh """#!/bin/bash
        # Script to create namespace, if doesnot exists
        #!/bin/bash
        #namespace_name="boutique"
        echo "Namespace Provided is ${namespace_name}"
        # Validate if the namespace exists
        if kubectl get ns "${namespace_name}" &> /dev/null ; then 
        echo "Your Namespace '${namespace_name}' exists!!!!!!"
        exit 0
        else
        echo "Your namespace '${namespace_name}' doesnot exists, so creating it!!!!!!"
        if kubectl create ns '${namespace_name}' &> /dev/null; then
          echo "Your namespace '${namespace_name}' has created succesfully"
          exit 0
        else 
          echo "Some error , failed to create '${namespace_name}'"
          exit 1
        fi
        fi
        """
    }

    def defaultdeny_netpol_cration(namespace_name, label_name, fileName){
    jenkins.sh """#!/bin/bash
    # Script to create networkpolicy
    #!/bin/bash
    echo "Default Network Policy creating for the NAMESPACE ${namespace_name}"
    # label the Namespace
    kubectl label ns '${namespace_name}' namespace=${label_name}
    sed -i "s|NS|${namespace_name}|g" $fileName
    sed -i "s|LN|${label_name}|g" $fileName
    cat $fileName
    kubectl apply -f $fileName
    """
    }
}    