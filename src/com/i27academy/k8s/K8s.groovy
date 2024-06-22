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

    def imageBuildFrontEnd(appName) {
       jenkins.sh """
        echo "**************************** Building Docker Image ****************************"
        sh "docker build --force-rm --no-cachet ${env.DOCKER_HUB}/${appName}:${GIT_COMMIT} ./.cicd"
        echo "********Docker login******"
        sh "docker login -u ${DOCKER_CREDS_USR} -p ${DOCKER_CREDS_PSW}"
        echo "********Docker Push******"
        sh "docker push ${env.DOCKER_HUB}/${appName}:${GIT_COMMIT}"
        """
    }
}    