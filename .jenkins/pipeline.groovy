pipeline {
    agent any
    tools { 
        maven 'Maven3' 
        jdk 'Java8' 
    }
    stages {
        stage ('Init') {
            steps {
                git url: "https://github.com/jochenw/ispm"
            }
        }

        stage ('Build') {
            steps {
                withMaven(
                     // Maven installation declared in the Jenkins "Global Tool Configuration"
                     maven: 'Maven3',

                    // Use `$WORKSPACE/.repository` for local repository folder to avoid shared repositories
                    mavenLocalRepo: '.repository',
                ) {
    			    bat 'mvn.cmd -fJava/ispm-core/pom.xml -Dmaven.test.failure.ignore=true clean install'
                }
            }
        }
    }
}
