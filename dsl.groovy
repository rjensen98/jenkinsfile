String jsonData = readFileFromWorkspace('jobs-config.json')
groovy.json.JsonSlurperClassic slurper = new groovy.json.JsonSlurperClassic()
def repos = slurper.parseText(jsonData)
repos.each {
    print "${it.key}"
}

def folderName = 'dsl-test'
folder(folderName) {
    views {
        listView('poc') {
            jobs {
              regex('.*-poc')  
            }
            columns{
                status()
                name()
                buildButton()
            }
        }
        listView('dev') {
            jobs {
              regex('.*-dev')  
            }
            columns{
                status()
                name()
                buildButton()
            }
        }
        listView('tst') {
            jobs {
              regex('.*-tst')  
            }
            columns{
                status()
                name()
                buildButton()
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

def testEchoVar = 'hello world'

repos.each { repo ->
  print "Cloning ${repo.url}/${repo.branch} -> ${repo.name}"
  ["git", "clone", "--branch", repo.branch, repo.url, repo.name].execute()
  print "Clone complete. Files: "
  ["ls", "-R"].execute()
  print "Evaluating jenkinsfile..."
  evaluate(new File("${repo.name}/${repo.script}"))
}

// jobs.each { job ->
//   pipelineJob("${folderName}/${job.name}") {
//     definition {
//       cpsScm {
//         scm {
//           git {
//             remote {
//               name('origin')
//               url(job.url)
//               credentials(job.cred)
//             }
//             branch(job.branch)
//           }
//         }
//         scriptPath(job.script)
//       }
//     }
//   }
// }

