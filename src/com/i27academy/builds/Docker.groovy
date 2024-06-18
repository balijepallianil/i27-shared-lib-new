package com.i27academy.builds

class Docker {
    def jenkins
    Docker(jenkins) {
        this.jenkins = jenkins
    }

    def add(firstNumber, secondNumber) {
        return firstNumber+secondNumber
    }

    def buildApp(appName) {
        jenkins.sh """
        echo "Building the Maven for $appName application using Shared Library"
        mvn clean package -DskipTests=true
        """
    }

}