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
      
      stage ('Install NPM packages') {
        steps {
        	sh 'cd frontend && npm install'
        }
      }
      
      stage ('Build static files') {
        steps {
            sh 'cp /home/jenkins-static/.env frontend'
        	sh 'cd frontend && npm run build'
        }
      }
      
      stage ('Move distribution files') {
        steps {
        	sh 'cp -rf frontend/dist/* /home/jenkins-static/static'
        }
      }
      
    }
    post {   
      always {
        discordSend description: "Jenkins Pipeline Build", footer: "(Tests,Coverage and, Javadocs)", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: "https://discord.com/api/webhooks/932609436411899904/ak9f_1HNvhlYqxVSCV96ZmeIVt0aaYjFe8tKlgXwY-RPLYMmIp2UCpcT-Wc0qXlTLN-4"
      }        
    }
}
