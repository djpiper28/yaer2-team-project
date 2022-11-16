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
      
        stage ('Compile') {
            steps {
                sh 'cd real-time-order/ && go build'
            } 
        }
      
        stage ('Test') {
            steps {
                sh 'cd java-backend && python3 pre_pipeline.py'
                sh 'cd real-time-order/ && python3 init_tests.py && go test ./...'
            } 
        }
        stage ('Format') {
          steps {
                sh 'cd real-time-order/ && bash format.sh'
          }
        }
    }
    post {   
      always {
          discordSend description: "Jenkins Pipeline Build", footer: "(Compile)", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: "https://discord.com/api/webhooks/932609436411899904/ak9f_1HNvhlYqxVSCV96ZmeIVt0aaYjFe8tKlgXwY-RPLYMmIp2UCpcT-Wc0qXlTLN-4"
      }        
    }
}
