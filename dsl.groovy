String jsonData = readFileFromWorkspace('jobs-config.json')
groovy.json.JsonSlurperClassic slurper = new groovy.json.JsonSlurperClassic()
def repos = slurper.parseText(jsonData)

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

repos.each { repo ->
  //println "Cloning ${repo.url}/${repo.branch} -> ${repo.name}"
  def clone = ["git", "clone", "--branch", repo.branch, repo.url, repo.name].execute()
  println clone.text
  println "Clone complete. Files: "
  //def list = ["ls", "-R"].execute()
  //println list.text
  println "Evaluating jenkinsfile..."
  evaluate(new File("${repo.name}/${repo.script}"))
}

