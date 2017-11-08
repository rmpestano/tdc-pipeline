pipeline {
    agent any

    stages {

        stage('checkout') {

            steps {
                git 'https://github.com/rmpestano/tdc-pipeline.git'
            }
        }

        stage('build') {

            steps {
                sh 'mvn clean package'
            }

        }
    }

}