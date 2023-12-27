pipeline {
    agent { label 'slave003' }
    parameters {
        booleanParam(name: 'scanOnly', defaultValue: true, description: 'Führt nur den NexusIQ-Scan aus, ohne neu zu bauen.')
        booleanParam(name: 'newVersion', defaultValue: false, description: 'Muss gesetzt werden, wenn eine neue Version gebaut werden soll.') 
    }
    tools {
        maven 'Maven'
        jdk "JDK 1.8"
    }
    triggers {
        cron('5 8 * * 3')
    }
    stages {
        stage("Checkout") {
            steps {
                script {
                    checkout scmGit(branches: [[name: '*/tomee-8.x']], browser: github('https://github.com/CSSAG/tomee'), extensions: [], userRemoteConfigs: [[credentialsId: 'Github-dovogt', url: 'https://github.com/CSSAG/tomee.git']])
                }
            }
        }
        stage("Build TomEE"){
            when { expression { ! params.scanOnly } }
            steps {
                script {
                    if (params.newVersion) {
                        sh "mvn clean install -e -pl tomee/apache-tomee -am"
                    } else {
                        sh "mvn -Pquick -Dsurefire.useFile=false -DdisableXmlReport=true -DuniqueVersion=false -ff -Dassemble -DskipTests -DfailIfNoTests=false clean install"
                    }
                }
            }
        }
        stage("Scan TomEE"){
            steps {
                script {
                    def applicationId = "css-tomee"
                    nexusPolicyEvaluation advancedProperties: '', enableDebugLogging: true, failBuildOnNetworkError: true, iqApplication: manualApplication(applicationId), iqInstanceId: 'nexus-iq', iqOrganization: '951c4423a0d4439d8721ad15886d9f4c', iqScanPatterns: [[scanPattern: '**/tomee/apache-tomee/target/apache-tomee-webprofile-*.tar.gz']], iqStage: 'build', jobCredentialsId: ''

                }
            }
        }
    }

    post {
        failure {
            script {
                emailext body: 'Die Pipeline TomEE-CSS ist fehlgeschlagen.', subject: 'Die Pipeline TomEE-CSS ist fehlgeschlagen.', to: 'build-info@css.de'
            }
        }
    }
    
}
