pipeline {
    agent any

    stages {

     stage('build') {

        steps {
            sh 'mvn clean package'
        }
    }

    stage('Deploy') {
        steps {
            sh 'docker stop tdc-pipeline || true && docker rm tdc-pipeline || true'
            sh 'docker build -t tdc-pipeline .'
            sh 'docker run -d --name tdc-pipeline -p 8181:8080 -v ~/db:/opt/jboss/db tdc-pipeline &'
        }
    }
  }
}