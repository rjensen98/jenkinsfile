// pipeline definition API can be found at http://adam-ci.itamswarm.rgare.net/plugin/job-dsl/api-viewer/index.html


folder("${PARENT_FOLDER}")
pipelineJob("${PARENT_FOLDER}/${JOB_NAME}-${JOB_ENV}") {
    parameters {
        booleanParam('FLAG', true)
        choiceParam('OPTION', ['option 1 (default)', 'option 2', 'option 3'])
    }
    properties {
        buildDiscarder {
            strategy {
                logRotator {
                    daysToKeepStr('350')
                    numToKeepStr('50')
                    artifactDaysToKeepStr('')
                    artifactNumToKeepStr('')
                }
            }
        }
    }
    definition {
        cpsScm {
            scriptPath('jenkinsfile-for-dsl')
            scm {
                git {
                    remote {
                        url("${JOB_REPO_URL}")
                        credentials("${JOB_REPO_CREDS}")
                    }
                    branches('master')
                    extensions {
                        cloneOptions { depth(1) }
                    }
                }
            }
        }
    }
}

