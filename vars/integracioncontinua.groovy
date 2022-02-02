def call(Map pipelineParameters) {
    pipeline {
        agent any
        environment {
            NEXUS_USER = credentials('usernexusadmin')
            NEXUS_PASSWORD = credentials('passnexusadmin')
            VERSION = '0.0.17'
            FINAL_VERSION = '1.0.0'
        }
        stages {
            stage('-1 logs') {
                steps {
                    sh "echo 'branchname: '" + BRANCH_NAME
                //sh 'printenv'
                }
            }

            stage('0 Validate Maven Files') {
                when {
                        anyOf {
                                not { expression { fileExists ('pom.xml') } }
                                not { expression { fileExists ('mvnw') } }
                        }
                }
                    steps {
                        sh "echo  'Faltan archivos Maven en su estructura'"
                        script {
                            error('file dont exist :( ')
                        }
                    }
            }
            stage('1 Compile') {
                //- Compilar el código con comando maven
                steps {
                    sh "echo 'Compile Code!'"
                // Run Maven on a Unix agent.
                //sh "mvn clean compile -e"
                }
            }
            stage('2 Unit Test') {
                //- Testear el código con comando maven
                steps {
                    sh "echo 'Test Code!'"
                // Run Maven on a Unix agent.
                //sh "mvn clean test -e"
                }
            }
            stage('3 Build jar') {
                //- Generar artefacto del código compilado.
                steps {
                    sh "echo 'Build .Jar!'"
                // Run Maven on a Unix agent.
                //sh "mvn clean package -e"
                }
            }
            stage('4 SonarQube') {
                //- Generar análisis con sonar para cada ejecución
                //- Cada ejecución debe tener el siguiente formato de nombre: QUE ES EL NOMBRE DE EJECUCIÓN ??
                //- {nombreRepo}-{rama}-{numeroEjecucion} ejemplo:
                //- ms-iclab-feature-estadomundial(Si está usando el CRUD ms-iclab-feature-[nombre de su crud])
                sh "echo 'SonarQube'"
                steps {
                //                    withSonarQubeEnv('sonarqube') {
                //                        sh "echo 'SonarQube'"
                //                        sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=covid-devops'
                //                    }
                }
                post {
                    //- Subir el artefacto creado al repositorio privado de Nexus.
                    //- Ejecutar este paso solo si los pasos anteriores se ejecutan de manera correcta.
                    success {
                        sh "echo 'Subir a nexus'"
                    //                         nexusPublisher nexusInstanceId: 'nexus',
                    //                         nexusRepositoryId: 'devops-usach-nexus',
                    //                        packages: [[$class: 'MavenPackage',
                    //                             mavenAssetList: [[classifier: '',
                    //                                             extension: '',
                    //                                             filePath: 'build/DevOpsUsach2020-0.0.1.jar']],
                    //                             mavenCoordinate: [artifactId: 'DevOpsUsach2020',
                    //                                             groupId: 'com.devopsusach2020',
                    //                                             packaging: 'jar',
                    //                                             version: VERSION]]]
                    }
                }
            }
            stage('6 gitCreateRelease') {
                //- Crear rama release cuando todos los stages anteriores estén correctamente ejecutados.
                //- Este stage sólo debe estar disponible para la rama develop.
                when {
                    branch 'develop'
                }
                steps {
                    sh "echo 'gitCreateRelease'"
                    withCredentials([gitUsernamePassword(credentialsId: 'github-token')]) {
                            sh '''
                            git checkout -b test-crearRama
                            git push origin test-crearRama
                            '''
                    }
                //solo cuando es develop debo crear rama release.
                }
            }
        }
        post {
            success {
                    slackSend color: 'good', message: "[Grupo5][PIPELINE IC][${env.BRANCH_NAME}][Stage: ${BUILD_ID}][Resultado: Ok]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'slack-duribef'
            }
            failure {
                    slackSend color: 'danger', message: "[Grupo5][PIPELINE IC][${env.BRANCH_NAME}][Stage: ${BUILD_ID}][Resultado: No OK]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'slack-duribef'
            }
        }
    }
}
