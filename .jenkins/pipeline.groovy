pipeline {
    agent any
    tools { 
        maven 'Maven3' 
        jdk 'Java8' 
    }
    stages {
        stage ('Init') {
            steps {
            }
        }

        stage ('Build') {
            steps {
				sh 'mvn -fJava/ispm-core/pom.xml -Dmaven.test.failure.ignore=true clean install'
            }
        }
    }
}
