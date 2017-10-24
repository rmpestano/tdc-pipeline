pipeline {
    agent any
    stages {
        /* stage('Checkout') {
             steps {
                 git 'https://github.com/rmpestano/tdc-pipeline.
             }
             }*/
        stage('build') {
            steps {
                sh 'mvn clean package -DskipTests'
                stash includes: 'src/**, pom.xml, Dockerfile, docker/**', name: 'src'
            }
        }

        stage('unit-tests') {
            steps {
                sh 'mvn test -Pcoverage'
                stash includes: 'src/**, pom.xml, Dockerfile, target/**', name: 'unit' //save because of coverage re usage in it-tests stage
            }
        }

        stage('tests') {
            failFast true

            parallel {

                stage('it-tests') {
                    /*agent {
                        docker {
                            image 'maven:3.3.9-alpine'
                            args '-v $HOME/.m2:/root/.m2 -v $HOME/db:/root/db'
                        }
                        }*/
                    steps {
                        dir('it-tests') {
                            //sh 'rm -r *'
                            unstash 'unit'
                            sh "ls -la ${pwd()}"
                            sh 'mvn flyway:clean flyway:migrate -Pmigrations -Ddb.name=cars-test'
                            sh 'mvn test -Pit-tests -Darquillian.port-offset=100 -Darquillian.port=10090 -Pcoverage -Djacoco.destFile=jacoco-it'
                            sh "ls -la ${pwd()}"
                            withSonarQubeEnv('sonar') {
                                sh 'mvn sonar:sonar'
                            }
                            livingDocs(featuresDir: 'target')

                        }

                    }
                }

                stage('ft-tests') {
                    steps {
                        dir('ft-tests') {
                            unstash 'src'
                            sh 'mvn flyway:clean flyway:migrate -Pmigrations -Ddb.name=cars-ft-test'
                            sh 'mvn test -Pft-tests -Darquillian.port-offset=120 -Darquillian.port=10110 -Darquillian.container=wildfly:10.1.0.Final:managed'
                        }
                    }
                }
            }

        }

        stage("Quality Gate") {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
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
        }

        stage('deploy to QA') {
            steps {
                dir("QA") {
                    unstash 'src'
                    sh "ls -la ${pwd()}"
                    sh 'docker stop tdc-pipeline-qa || true && docker rm tdc-pipeline-qa || true'
                    sh 'mvn clean package -DskipTests flyway:clean flyway:migrate -P migrations -Ddb.name=cars-qa'
                    sh 'docker build -t tdc-pipeline-qa .'
                    sh 'docker run --name tdc-pipeline-qa -p 8282:8080 -v ~/db:/opt/jboss/db tdc-pipeline-qa &'
                }
            }
        }

        stage('Go to production?') {
            agent none
            steps {
                script {
                    timeout(time: 1, unit: 'DAYS') {
                        input message: 'Approve deployment?'
                    }
                }
            }
        }

        stage('migrations') {
            steps {
                sh 'docker stop tdc-pipeline || true && docker rm tdc-pipeline || true'
                sh 'mvn flyway:repair flyway:migrate -P migrations'
            }
        }

        stage('deploy to production') {
            steps {
                sh 'docker build -t tdc-pipeline .'
                sh 'docker run --name tdc-pipeline -p 8181:8080 -v ~/db:/opt/jboss/db tdc-pipeline &'
            }
        }

        stage('smoke-tests') {
            steps {
                sh 'mvn test -Psmoke -DAPP_CONTEXT=http://localhost:8181/tdc-pipeline/rest/health'
            }
        }

        stage('perf-tests') {
            steps {
                script {
                    try {
                        sh 'mvn gatling:execute -Pperf -DAPP_CONTEXT=http://localhost:8181/tdc-pipeline/'
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