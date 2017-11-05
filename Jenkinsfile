pipeline {
    agent any


    stages {
        /* stage('Checkout') { //not needed because we checkout pipeline from SCM 
             steps {
                 git 'https://github.com/rmpestano/tdc-pipeline.
             }
             }*/
             stage('build') {
                steps {
                    sh 'mvn clean package -DskipTests'
                    stash includes: 'src/**, pom.xml, Dockerfile, docker/**, target/**', name: 'src' // saves sources to avoid rebuild in stages that run in separated dir
            }
        }

        stage('unit-tests') {
            steps {
                sh 'mvn test -Pcoverage'
                stash includes: 'src/**, pom.xml, Dockerfile, target/**', name: 'unit' //save because of coverage re usage in 'it-tests' stage
            }
        }

        stage('Parallel tests') {
            failFast true // first to fail abort parallel execution

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
                            //sh 'rm -r *' do not clear folder to avoid unpacking arquillian server
                            unstash 'unit' //copy from unit tests because it generates coverage info (jacaco.exec)
                            sh 'mvn flyway:clean flyway:migrate -Pmigrations -Ddb.name=cars-test'
                            sh 'mvn test -Pit-tests -Darquillian.port-offset=100 -Darquillian.port=10090 -Pcoverage -Djacoco.destFile=jacoco-it'
                            stash includes: 'src/**, pom.xml, target/**', excludes: 'target/server/**', name: 'it' //saves 'it' artifacts to use in 'Quality Gate' stage
                        }

                    }
                }

                stage('ft-tests') {
                    steps {
                        dir('ft-tests') {
                            unstash 'src'
                            sh 'mvn flyway:clean flyway:migrate -Pmigrations -Ddb.name=cars-ft-test'
                            sh 'mvn test -Pft-tests -Darquillian.port-offset=120 -Darquillian.port=10110'
                        }
                    }
                }
            }

        }

        stage("SonarQube analysis") {
            steps {
                dir("sonar") {
                    unstash 'it'
                    withSonarQubeEnv('sonar') {
                        sh 'mvn sonar:sonar'
                    }
                }
            }
        }

        stage("Living docs") {
           steps {
            dir("docs") {
                    unstash 'it' //loads 'it' folder because bdd tests are executed in 'it' stage 
                    livingDocs(featuresDir: 'target') 
                }
            }
        }

        stage("Quality Gate") {
            steps {
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

            stage('Deploy to QA') {
                steps {
                    dir("QA") {
                    unstash 'src' //uses the same source from first stage
                    sh 'docker stop tdc-pipeline-qa || true && docker rm tdc-pipeline-qa || true'
                    sh 'mvn clean package -DskipTests flyway:clean flyway:migrate -P migrations -Ddb.name=cars-qa' //needs to rebuild because 'db.name' is different
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

        stage('Migrations') {
            steps {
                sh 'docker stop tdc-pipeline || true && docker rm tdc-pipeline || true'
                sh 'mvn flyway:repair flyway:migrate -P migrations'
            }
        }

        stage('Deploy to production') {
            steps {
                sh 'docker build -t tdc-pipeline .'
                sh 'docker run --name tdc-pipeline -p 8181:8080 -v ~/db:/opt/jboss/db tdc-pipeline &'
            }
        }

        stage('Smoke tests') {
            steps {
                sh 'mvn test -Psmoke-tests -DAPP_CONTEXT=http://localhost:8181/tdc-pipeline/rest/health'
            }
        }

        stage('Perf tests') {
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