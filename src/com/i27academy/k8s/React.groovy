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
}    