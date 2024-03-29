/**
 *  DSL_SEED.GROOVY
 *
 *    DSL job to create Jenkins pipeline jobs based on individual project's pipeline definitions
 *
 *    Prerequisites:
 *        - Jenkins must have a global environment variable, "PIPELINE_JOB_ENVIRONMENTS", defined
 *            - this is a CSV list of environments that this Jenkins server is integrated with
 *              (i.e. "poc,dev,uat")
 *        - There must be a jobs-config.json in the same repo as this DSL file
 *            - file contains an array of job configurations to be implemented
 *            - structure:
 *                [
 *                    {
 *                        "name": "name-of-project-to-be-created",
 *                        "script": "project-dsl.groovy",  // groovy script within the project's own repo that defines how the pipeline job should be configured
 *                        "url": "repository-url-for-child-project.git",
 *                        "branch": "branch-to-clone-child-project-from",
 *                        "cred": "jenkins-credential-id-for-access-to-child-project-repo"
 *                    }
 *                ]
 *    Optional:
 *        - "folderName" variable below may be modified to specify the project folder name this DSL will configure
 */


import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import hudson.model.*

// Modify this for an alternate project name
def folderName = 'Adam'

// Get list of managed environments from Jenkins' PIPELINE_JOB_ENVIRONMENTS environment variable
// NOTE: PIPELINE_JOB_ENVIRONMENTS **MUST** be set within Manage Jenkins -> Configure System >> Global Properties >> Environment variables
def jobEnvs = "${PIPELINE_JOB_ENVIRONMENTS}".split(',')

// Get job configuration data
String jsonData = readFileFromWorkspace('jobs-config.json')
groovy.json.JsonSlurperClassic slurper = new groovy.json.JsonSlurperClassic()
def repos = slurper.parseText(jsonData)

// Create folders and views
folder(folderName) {
    views {
        for (String jobEnv : jobEnvs) {
            listView(jobEnv) {
                jobs {
                    regex(".*-${jobEnv}")
                }
                columns {
                    status()
                    name()
                    buildButton()
                }
            }
        }
        listView('DependencyChecks') {
            jobs {
                regex('.*-dependency-checker')
            }
            columns {
                status()
                name()
                buildButton()
            }
        }
    }
}

// Loop through job configurations to set each one up
repos.each { repo ->
    println "Deleting ${WORKSPACE}/${repo.name}..."
    ["rm", "-Rf", "${WORKSPACE}/${repo.name}"].execute().waitFor()

    println "Cloning ${repo.url}/${repo.branch} -> ${repo.name}"
    ["git", "clone", "--branch", repo.branch, repo.url, "${WORKSPACE}/${repo.name}"].execute().waitFor()

    // Dynamically run child project groovy DSL definitions
    // (https://devops.datenkollektiv.de/from-plain-groovy-to-jenkins-job-dsl-a-quantum-jump.html)
    def jobDslScript = new File("${WORKSPACE}/${repo.name}/${repo.script}")
    def workspace = new File('.')

    // Create a job for each environment defined by Jenkins' Global "PIPELINE_JOB_ENVIRONMENTS" env variable
    for (String jobEnv : jobEnvs) {
        println "Setting up env: ${jobEnv}"

        // Pass some variables to the child DSL script
        def bindings = [JOB_ENV: jobEnv, JOB_NAME: repo.name, JOB_REPO_URL: repo.url, JOB_REPO_CREDS: repo.cred, PARENT_FOLDER: folderName]
        def jobManagement = new JenkinsJobManagement(System.out, bindings, workspace)

        println "Executing script: ${repo.name}/${repo.script}"
        // https://github.com/jenkinsci/job-dsl-plugin/blob/350005c9878ab4fd5210667c25b9f555b230fdae/job-dsl-core/src/main/groovy/javaposse/jobdsl/dsl/AbstractDslScriptLoader.groovy#L251
        new DslScriptLoader(jobManagement).runScript(jobDslScript.text)
    }

    // Final clean-up
    println "Deleting ${WORKSPACE}/${repo.name} again..."
    ["rm", "-Rf", "${WORKSPACE}/${repo.name}"].execute().waitFor()
}


//        import javaposse.jobdsl.plugin.LookupStrategy
//        import hudson.FilePath
//        def currentBuild = Thread.currentThread().executable  // hudson.model.FreeStyleBuild
//        def jobManagement = new JenkinsJobManagement(System.out, bindings, currentBuild, new FilePath(new File('.')), LookupStrategy.JENKINS_ROOT)

