def call(Map pipelineParameters) {
    pipeline {
        agent any
        environment {
            NEXUS_USER = credentials('usernexusadmin')
            NEXUS_PASSWORD = credentials('passnexusadmin')
            SLACK_TOKEN = 'slack-duribef'
            VERSION = '0.0.17'
            FINAL_VERSION = '1.0.0'
            STAGE = ' '
        }
        stages {
            // stage('gitDiff') {
            //     //- Mostrar por pantalla las diferencias entre la rama release en curso y la rama
            //     //master.(Opcional)
            //     steps {
            //         script { STAGE = 'gitDiff ' }
            //         sh 'echo gitDiff'
            //         sh 'git diff release/release-v1-0-0 origin/main'
            //     }
            // }
            // stage('nexusDownload') {
            //     //- Descargar el artefacto creado al workspace de la ejecución del pipeline.
            //     steps {
            //         script { STAGE = 'nexusDownload ' }
            //         sh 'sleep 5 '
            //         sh 'echo nexusDownload'
            //         sh 'curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/$VERSION/DevOpsUsach2020-$VERSION.jar -O'
            //     }
            // }
            // stage('Run Jar') {
            //     //- Ejecutar artefacto descargado.
            //     steps {
            //         script { STAGE = 'Run Jar ' }
            //         sh 'echo Run Jar'
            //         sh 'nohup java -jar DevOpsUsach2020-$VERSION.jar & >/dev/null'
            //     }
            // }
            stage('test') {
                //- Realizar llamado a microservicio expuesto en local para cada uno de sus
                //métodos y mostrar los resultados.
                steps {
                    script { STAGE = 'test ' }
                    sh 'echo Test Curl'
                // sh "sleep 30 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
                }
                post {
                    success {
                        script {
                            STAGE = 'gitTagMaster '
                            sh 'echo "gitTagMaster"'
                        }
                        withCredentials([gitUsernamePassword(credentialsId: 'github-token')]) {
                            sh '''
                            git tag -a "v1-0-0" -m "Release 1-0-0"
                            git push origin "v1-0-0"
                            git show v1-0-0
                            '''
                        }
                        script {
                            STAGE = 'gitMergeMaster '
                            sh 'echo "gitMergeMaster" '
                        }
                        withCredentials([gitUsernamePassword(credentialsId: 'github-token')]) {
                            sh '''
                            git checkout main2
                            git merge release/release-v1-0-0
                            git push origin main2
                            git tag
                            '''
                        }
                        script {
                            STAGE = 'gitMergeDevelop '
                            sh "echo 'gitMergeDevelop'"
                        }
                        withCredentials([gitUsernamePassword(credentialsId: 'github-token')]) {
                            sh '''
                            git checkout develop2
                            git merge release/release-v1-0-0
                            git push origin main2
                            git tag
                            '''
                        }
                    }
                }
            }
        }
            post {
                success {
                    slackSend(
                        color: 'good',
                        message: "[Grupo5][PIPELINE Release][${env.BRANCH_NAME}][Stage: ${STAGE}][Resultado: Ok]",
                        tokenCredentialId: SLACK_TOKEN)
                }
                failure {
                    slackSend(
                        color: 'danger',
                        message: "[Grupo5][PIPELINE Release][${env.BRANCH_NAME}][Stage: ${STAGE}][Resultado: No OK]",
                        tokenCredentialId: SLACK_TOKEN)
                }
            }
    }
}
