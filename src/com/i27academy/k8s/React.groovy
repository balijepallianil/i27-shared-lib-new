package com.i27academy.k8s

class React {
    def jenkins
    React(jenkins) {
        this.jenkins = jenkins
    }
    def k8sdeploy(fileName, namespace , docker_image){
        jenkins.sh """
        echo "Executing K8S Deploy Method"
        sed -i "s|DIT|${docker_image}|g" ./.cicd/$fileName
        kubectl apply -f ./.cicd/$fileName -n $namespace
        """
    }
}    