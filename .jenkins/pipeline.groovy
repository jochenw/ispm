pipeline {
	stages {
		stage('build') {
            steps {
				sh 'mvn -Pjacoco -f Java/ispm-core/pom.xml clean install'
			}
		}
	}
}
