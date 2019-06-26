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
  def list2 = ["ls", "-R", "/${repo.name}"].execute()
  println list2.text
  ["rm", "-Rf", repo.name].execute().text
  //println "Cloning ${repo.url}/${repo.branch} -> ${repo.name}"
  ["git", "clone", "--branch", repo.branch, repo.url, repo.name].execute().text
  println "Clone complete. Files: "
  def list = ["ls", "-R", "/${repo.name}"].execute()
  println list.text
  println "Evaluating jenkinsfile..."
  evaluate(new File("${repo.name}/${repo.script}"))
}

