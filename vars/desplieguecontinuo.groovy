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
            stage('gitDiff') {
                //- Mostrar por pantalla las diferencias entre la rama release en curso y la rama
                //master.(Opcional)
                steps {
                    script { STAGE = 'gitDiff ' }
                    sh '''
                      echo 'gitDiff'
#                     git diff release-v1-0-0 origin/main
                      '''
                }
            }
            stage('nexusDownload') {
                //- Descargar el artefacto creado al workspace de la ejecución del pipeline.
                steps {
                    script { STAGE = 'nexusDownload ' }
                    sh 'sleep 5 '
                    sh 'echo nexusDownload'
                //sh 'curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/$VERSION/DevOpsUsach2020-$VERSION.jar -O'
                }
            }
            stage('Run Jar') {
                //- Ejecutar artefacto descargado.
                steps {
                    script { STAGE = 'Run Jar ' }
                    sh 'echo Run Jar'
                //                  sh 'nohup java -jar DevOpsUsach2020-$VERSION.jar & >/dev/null'
                }
            }
            stage('test') {
                //- Realizar llamado a microservicio expuesto en local para cada uno de sus
                //métodos y mostrar los resultados.
                steps {
                    script { STAGE = 'test ' }
                    sh 'echo Test Curl'
                //                  sh "sleep 30 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
                }
            }
            stage('gitMergeMaster') {
                //- Realizar merge directo hacia la rama master.
                //- Ejecutar sólo si todo lo demás resulta de forma exitosa.
                steps {
                    script { STAGE = 'gitMergeMaster ' }
                    sh 'echo gitMergeMaster'
                //                    withCredentials([gitUsernamePassword(credentialsId: 'github-token')]) {
                //                            sh '''
                //                            git checkout origin/test-crearRama
                //                            git merge release-v1-0-0
                //                            git commit -am "Merged release-v1-0-0 branch to test-crearRama"
                //                            git push origin HEAD:test-crearRama
                //                           '''}
                }
                post {
                    success {
                        script {
                            STAGE = 'gitMergeDevelop '
                            sh "echo 'gitMergeDevelop'"
                        }
                        script {
                            STAGE = 'gitTagMaster '
                            sh "echo 'gitTagMaster'"
                        }
                    }
                }
            }
            post {
                success {
                    slackSend(
                        color: 'good',
                        message: "[Grupo5][PIPELINE Release][${env.BRANCH_NAME}][Stage: ${BUILD_ID}][Resultado: Ok]",
                        tokenCredentialId: SLACK_TOKEN)
                }
                failure {
                    slackSend(
                        color: 'danger',
                        message: "[Grupo5][PIPELINE Release][${env.BRANCH_NAME}][Stage: ${BUILD_ID}][Resultado: No OK]",
                        tokenCredentialId: SLACK_TOKEN)
                }
            }
        }
    }
}
