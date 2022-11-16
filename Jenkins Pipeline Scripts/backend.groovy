pipeline {
    agent any
    
    stages {
        stage ('Set Git Config') {
            steps {
                sh 'git config --global credential.username jenkins-djpiper28'
                sh 'git config --global user.name jenkin-djpiper28'
                sh 'git config --global user.email djpiper28@gmail.com'
            }
        }
      
       stage ('Prep Dev Env') {
            steps {
                sh 'cd java-backend && python3 pre_pipeline.py'
            }
       }
        
        stage ('Gradle Tests') {
            steps {
                sh 'cd java-backend && gradle clean test || true'
            }
        }
      
        stage ('Javadoc') {
            steps {
                sh 'cd java-backend && gradle javadoc || true'
                javadoc(javadocDir: 'java-backend/app/build/docs/javadoc', keepAll: false)
            }
        }
        
        stage ('Format Code') {
            steps {
                sh 'cd java-backend && GIT_SSH_COMMAND="ssh -i /var/lib/jenkins/.ssh/id_ed25519 -o StrictHostKeyChecking=no" bash format.sh'
            }
        }
    }
    post {   
      always {
        junit '**/build/**/*.xml'
        publishCoverage adapters: [jacoco(execPattern: '**/build/**/*.exec')]
        discordSend description: "Jenkins Pipeline Build", footer: "(Tests,Coverage and, Javadocs)", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: "https://discord.com/api/webhooks/932609436411899904/ak9f_1HNvhlYqxVSCV96ZmeIVt0aaYjFe8tKlgXwY-RPLYMmIp2UCpcT-Wc0qXlTLN-4"
      }        
    }
}
