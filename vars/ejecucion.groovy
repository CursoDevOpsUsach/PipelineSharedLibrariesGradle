
  
def call(){
    pipeline {
        agent any
        environment {
            NEXUS_USER         = credentials('user-nexus')
            NEXUS_PASSWORD     = credentials('password-nexus')
        }
        stages {
            stage("Pipeline"){
                steps {
                    script{
                        env.STAGE  = ""
                        if ((env.BRANCH_NAME =~ '.*feature.*').matches() || (env.BRANCH_NAME =~ '.*develop.*').matches() ) {
                             echo "Rama Feature o develop"
                            integracioncontinua.call(
                                VERSION:"0.0.16"
                            )
                        } else if ((env.BRANCH_NAME =~ '.*release.*').matches()) {
                            echo "Rama Release"
                            desplieguecontinuo.call(
                                VERSION:"0.0.16"
                            )
                        } else {
                            echo "Su rama tiene formato erroneo o esta intentando ejecutar desde la rama master."
                            integracioncontinua.call(
                                VERSION:"0.0.16"
                            )
                        }
                                            }
                }
                post {
                    success {
                            slackSend color: 'good', message: "[Grupo5][PIPELINE IC][${env.BRANCH_NAME}][Stage: ${STAGE_NAME}][Resultado: Ok]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'slack-duribef'
                    }
                    failure {
                            slackSend color: 'danger', message: "[Grupo5][PIPELINE IC][${env.BRANCH_NAME}][Stage: ${STAGE_NAME}][Resultado: No OK]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'slack-duribef'
                    }
                }
            }
        }
    }
}
