pipeline {
    agent any
    tools { 
        maven 'Maven3' 
        jdk 'Java8' 
    }
    stages {
        stage ('Build') {
            steps {
                withMaven(
                     // Maven installation declared in the Jenkins "Global Tool Configuration"
                     maven: 'Maven3',

                    // Use `$WORKSPACE/.repository` for local repository folder to avoid shared repositories
                    mavenLocalRepo: '.repository',
                ) {
    			    bat 'mvn.cmd -fJava/ispm-core/pom.xml -Pjacoco -Dmaven.test.failure.ignore=true clean install'
                }
            }
        }
        stage ('Profile') {
            steps {
                jacoco( 
                    execPattern: 'Java/ispm-core/target/*.exec',
                    classPattern: 'Java/ispm-core/target/classes',
                    sourcePattern: 'Java/ispm-core/src/main/java',
                    exclusionPattern: 'src/test*'
                )
            }
        }
    }
}
