pipeline {
    agent any
    tools { 
        maven 'Maven3' 
        jdk 'Java8' 
    }
    stages {
        stage ('Init') {
            git url: "https://github.com/jochenw/ispm"
        }

        stage ('Build') {
            steps {
				bat 'mvn.cmd -fJava/ispm-core/pom.xml -Dmaven.test.failure.ignore=true clean install'
            }
        }
    }
}
