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
                sh 'cd admin-console && mkdir -p build && cd build && cmake .. && cmake --build . -j'
            } 
        }
      
        stage ('Test') {
            steps {
                sh 'cd java-backend && python3 pre_pipeline.py'
                sh 'cd admin-console/build && ./admin-console test'
            } 
        }
      
        stage ('Format Code') {
            steps {
                sh 'cd admin-console && GIT_SSH_COMMAND="ssh -i /var/lib/jenkins/.ssh/id_ed25519 -o StrictHostKeyChecking=no" bash format.sh'
            }
        }
    }
    post {   
      always {
          discordSend description: "Jenkins Pipeline Build", footer: "(Compile)", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: "REDACTED"
      }        
    }
}
