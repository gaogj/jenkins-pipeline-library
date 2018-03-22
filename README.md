Jenkins-Pipeline-Library is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Jenkins-Pipeline-Library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Jenkins-Pipeline-Library. If not, see http://www.gnu.org/licenses/.

## Jenkins Pipeline Library

This repository is a subset from what is used by ABN AMRO Bank for creating [Jenkins Pipelines](https://jenkins.io/doc/book/pipeline/) using [Shared Libraries](https://jenkins.io/doc/book/pipeline/shared-libraries/).

As we borrow heavily from Jenkins' open source community, we aim to share without violating our internal policies.

## Terminology

* **MainLine**: a branch that is a main branch, **Master** in *Git* or **Trunk** in *Subversion*. 
* **Application Code**: 
* 

## Core library

The format for a Jenkins Pipeline Library is that library files are groovy files in src/** and that pipelines and pipeline methods are in vars/.

For sake of keeping the different parts together, in this repository there are the standard pipeline parts from both the core and other languages.

Initially, it is just the Java pipeline (vars/javaPipeline) but others will follow.

## Java Pipeline

See [Java Pipeline docs](docs/javaPipeline).

## Atomist

We're also participating in the [Atomist Alpha test](https://www.atomist.com/).

For this we have embedded the Atomist [Pipeline code](http://docs.atomist.com/getting-started/jenkins/) for Jenkins integration.
To use this, you can do the following:

```groovy
@Library(['github.com/abnamrocoesd/jenkins-pipeline-library']) _
pipeline {
    agent { label 'docker' }
    options {
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                git 'your amazing git project'
            }
        }
        stage('Prepare Workspace') {
            steps {
                notifyAtomist("UNSTABLE", "STARTED")
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
    }
    post {
        success {
            echo "SUCCESS"
        }
        unstable {
            echo "UNSTABLE"
        }
        failure {
            echo "FAILURE"
        }
        changed {
            echo "Status Changed: [From: $currentBuild.previousBuild.result, To: $currentBuild.result]"
        }
        always {
            script {
                def result = currentBuild.result
                if (result == null) {
                    result = "SUCCESS"
                }
                notifyAtomist(result)
            }
            echo "ALWAYS"
            step([$class: 'WsCleanup', notFailBuild: true])
        }
    }
}
```