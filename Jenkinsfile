pipeline {
    agent any
    stages {
        /* stage('Checkout') {
             steps {
                 git 'https://github.com/rmpestano/tdc-cars.
             }
         }*/
        stage('build') {
            steps {
                sh 'mvn clean package -DskipTests'
                archiveArtifacts artifacts: '**/*', fingerprint: true
            }
        }

        /*stage('tests') {
            failFast true

            parallel {*/

        stage('unit-tests') {
            steps {
                sh 'mvn test -Pcoverage'
            }
        }

        stage('it-tests') {
            /*agent {
                docker {
                    image 'maven:3.3.9-alpine'
                    args '-v $HOME/.m2:/root/.m2 -v $HOME/db:/root/db'
                }
            }*/
            steps {
                sh 'mvn flyway:clean flyway:migrate -Pmigrations -Ddb.name=cars-test'
                sh 'mvn test -Pit-tests -Darquillian.port-offset=100 -Darquillian.port=10090 -Pcoverage -Djacoco.destFile=jacoco-it'
                withSonarQubeEnv('sonar') {
                     sh 'mvn sonar:sonar'
                }
                livingDocs()
                sh 'mvn jacoco:report -Pcoverage'
            }
        }

        stage('ft-tests') {
            steps {
                sh 'mvn flyway:clean flyway:migrate -Pmigrations -Ddb.name=cars-ft-test'
                sh 'mvn test -Pft-tests -Darquillian.port-offset=120 -Darquillian.port=10110 -Darquillian.container=wildfly:10.1.0.Final:managed'
            }
        }
        /* }

         }*/

       /* stage("Quality Gate") {
            steps {
                timeout(time: 20, unit: 'MINUTES') {
                    script {
                        def result = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
                        if (result.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${result.status}"
                        } else {
                           echo "Quality gate passed with result: ${result.status}"
                        }
                    }
                }
            }
        } */

        stage('migrations') {
            steps {
                sh 'docker stop tdc-cars || true && docker rm tdc-cars || true'
                sh 'mvn flyway:repair flyway:migrate -P migrations'
            }
        }

        stage('deploy') {
            steps {
                sh 'docker build -t rmpestano/tdc-cars .'
                sh 'docker run --name tdc-cars -p 8181:8080 -v ~/db:/opt/jboss/db tdc-cars &'
            }
        }

        stage('smoke-tests') {
            steps {
                sh 'mvn test -Psmoke -DAPP_CONTEXT=http://localhost:8181/tdc-cars/rest/health'
            }
        }

        stage('perf-tests') {
            steps {
                script {
                    try {
                        sh 'mvn gatling:execute -Pperf -DAPP_CONTEXT=http://localhost:8181/tdc-cars/'
                    } finally {
                        gatlingArchive()
                    }
                }
            }
        }

    }

    post {
        always {
            lastChanges()
        }
        success {
            slackSend channel: '#builds',
                color: 'good',
                message: "${currentBuild.fullDisplayName} *succeeded*. (<${env.BUILD_URL}|Open>)"
        }
        failure {
            slackSend channel: '#builds',
                color: 'danger',
                message: "${currentBuild.fullDisplayName} *failed*. (<${env.BUILD_URL}|Open>)"
        }
    }
}