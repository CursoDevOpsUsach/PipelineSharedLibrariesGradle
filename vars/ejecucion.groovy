def call() {
    if ((env.BRANCH_NAME =~ '.*feature.*').matches() || (env.BRANCH_NAME =~ '.*develop.*').matches() ) {
        figlet 'Running CI'
        echo 'Rama Feature o develop'
        integracioncontinua.call()
} else if ((env.BRANCH_NAME =~ '.*release.*').matches()) {
        figlet 'Running CD'
        echo 'Rama Release'
        desplieguecontinuo.call()
} else {
        echo 'Su rama tiene formato erroneo o esta intentando ejecutar desde la rama master.'
    }
}
return this
