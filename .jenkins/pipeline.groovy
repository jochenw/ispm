pipeline {
    agent any
    tools { 
        maven 'Maven3' 
        jdk 'Java8' 
    }
    stages {
        stage ('Init') {
            steps {
                echo "Initializing pipeline."
            }
        }

        stage ('Build') {
            steps {
				bat 'mvn.cmd -fJava/ispm-core/pom.xml -Dmaven.test.failure.ignore=true clean install'
            }
        }
    }
}
