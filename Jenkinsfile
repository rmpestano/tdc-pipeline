pipeline {
    agent any

    stages {

     stage('build') {

        steps {
            sh 'mvn clean package -DskipTests'
        }
    }

   stage('unit-tests') {
          steps {
               sh 'mvn test -Pcoverage'
            }
        }

   stage("SonarQube analysis") {
        steps {
          withSonarQubeEnv('sonar') {
               sh 'mvn sonar:sonar'
            }
        }
     }

   stage("Quality Gate") {
        steps {
            sh 'sleep 12s'
            timeout(time: 5, unit: 'MINUTES') {
                script {
                    def result = waitForQualityGate()
                    if (result.status != 'OK') {
                        error "Pipeline aborted due to quality gate failure: ${result.status}"
                        } else {
                            echo "Quality gate passed with result: ${result.status}"
                        }
                    }
                }

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