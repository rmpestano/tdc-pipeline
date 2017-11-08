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
               sh 'mvn test'
            }
        }

  }

  post {
      always {
            sendNotification(currentBuild.result)
      }

      success {
           echo 'Build was a success'
      }

      failure {
           echo 'Build failure'
      }

      changed {
          echo 'Build status changed.'
      }
  }
}

def sendNotification(buildStatus) {

  buildStatus =  buildStatus ?: 'SUCCESSFUL'

  def color = buildStatus == 'SUCCESSFUL' ? 'good' : 'danger'

  def message = "${currentBuild.fullDisplayName} *${buildStatus}*. (<${env.BUILD_URL}|Open>)"


  slackSend (channel: '#builds', color: color, message: message)

}