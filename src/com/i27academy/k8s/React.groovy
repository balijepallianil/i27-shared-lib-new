package com.i27academy.k8s

class React {
    def jenkins
    React(jenkins) {
        this.jenkins = jenkins
    }
    def k8sdeploy(fileName, namespace , docker_image){
        jenkins.sh """
        ls -l
        echo "Executing K8S Deploy Method"
        sed -i "s|DIT|${docker_image}|g" $fileName
        kubectl apply -f $fileName -n $namespace
        """
    }
    def k8sHelmChartDeploy(appName, env, helmChartPath, imageTag, valuesfile){
        jenkins.sh """
        echo "Verifying if Chart Exists"
        if helm list | grep -q "${appName}-${env}-chart"; then 
          echo "This Charts Exists!!!!!!!"
          echo "Upgrading the Chart !!!!!"
          helm upgrade ${appName}-${env}-chart -f $valuesfile --set image.tag=${imageTag} ${helmChartPath}
        else
          echo "Chart doesnot exists !!!!!!"
          echo "Installing the Chart"
          helm install ${appName}-${env}-chart -f $valuesfile --set image.tag=${imageTag} ${helmChartPath}
        fi
        """
    }
    def gitclone() {
        jenkins.sh """
        echo "*******clone the Shared Library repo*****"
        git clone -b main https://github.com/balijepallianil/i27-shared-lib-new.git
        """
    }
}    