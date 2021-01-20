pipeline {
    agent any
    tools { 
        maven 'M3' 
        jdk 'jdk8' 
    }
    stages {
        stage ('Init') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                ''' 
            }
        }

        stage ('Build') {
            steps {
                echo 'This is a minimal pipeline.'
            }
        }
    }
}
