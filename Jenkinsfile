@Library('utils@master') _

def utils = new hee.tis.utils()

node {

    if (env.BRANCH_NAME != "master") {
        // Flagged as successful to avoid PRs appearing to fail checks.
        currentBuild.result = 'SUCCESS'
        return
    }

    def service = "usermanagement"

    deleteDir()

    stage('Checkout Git Repo') {
    checkout scm
    }

    env.GIT_COMMIT = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
    def mvn = "${tool 'Maven 3.3.9'}/bin/mvn"
    def workspace = pwd()
    def parent_workspace = pwd()
    def repository = "${env.GIT_COMMIT}".split("TIS-")[-1].split(".git")[0]
    def buildNumber = env.BUILD_NUMBER
    def buildVersion = env.GIT_COMMIT
    def imageName = ""
    def imageVersionTag = ""

    println "[Jenkinsfile INFO] Commit Hash is ${GIT_COMMIT}"

    milestone 1


    stage('Build') {
        sh "'${mvn}' clean install -DskipTests"
    }

    stage('Unit Tests') {
        try {
            sh "'${mvn}' clean test"
        } catch (err) {
            throw err
        } finally {
            junit '**/target/surefire-reports/TEST-*.xml'
        }
    }

    stage('Analyze Quality') {
      withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
        if (env.CHANGE_ID) {
          sh "'${mvn}' sonar:sonar -Dsonar.login='${SONAR_TOKEN}' -Dsonar.pullrequest.key=$env.CHANGE_ID"
        } else {
          sh "'${mvn}' sonar:sonar -Dsonar.login='${SONAR_TOKEN}' -Dsonar.branch.name=$env.BRANCH_NAME"
        }
      }
    }

    milestone 2

    stage('Dockerise') {
        env.VERSION = utils.getMvnToPom(workspace, 'version')
        env.GROUP_ID = utils.getMvnToPom(workspace, 'groupId')
        env.ARTIFACT_ID = utils.getMvnToPom(workspace, 'artifactId')
        env.PACKAGING = utils.getMvnToPom(workspace, 'packaging')
        // imageName = env.ARTIFACT_ID
        imageVersionTag = env.GIT_COMMIT


        imageName = service
        env.IMAGE_NAME = imageName

        try {
        sh "ansible-playbook -i $env.DEVOPS_BASE/ansible/inventory/dev $env.DEVOPS_BASE/ansible/tasks/spring-boot-build.yml"
        } catch (err) {
        throw err
        } finally {
        println "[Jenkinsfile INFO] Stage Dockerize completed..."
        }
    }

    if (env.BRANCH_NAME == "master") {

        milestone 3

        stage('Staging') {
            node {
                println "[Jenkinsfile INFO] Stage Deploy starting..."
                try {
                    sh "ansible-playbook -i $env.DEVOPS_BASE/ansible/inventory/stage $env.DEVOPS_BASE/ansible/${service}.yml --extra-vars=\"{\'versions\': {\'${service}\': \'${env.GIT_COMMIT}\'}}\""
                } catch (err) {
                    throw err
                } finally {
                    println "[Jenkinsfile INFO] Stage Dockerize completed..."
                }
            }
        }

        milestone 4

        stage('Approval') {
            timeout(time: 5, unit: 'HOURS') {
                input message: 'Deploy to production?', ok: 'Deploy!'
            }
        }

        milestone 5

        stage('Production') {
            node {
                try {
                    sh "ansible-playbook -i $env.DEVOPS_BASE/ansible/inventory/prod $env.DEVOPS_BASE/ansible/${service}.yml --extra-vars=\"{\'versions\': {\'${service}\': \'${env.GIT_COMMIT}\'}}\""
                    sh "ansible-playbook -i $env.DEVOPS_BASE/ansible/inventory/nimdta $env.DEVOPS_BASE/ansible/${service}.yml --extra-vars=\"{\'versions\': {\'${service}\': \'${env.GIT_COMMIT}\'}}\""
                } catch (err) {
                    throw err
                } finally {
                    println "[Jenkinsfile INFO] Stage Dockerize completed..."
                }
            }
        }
    }
}
