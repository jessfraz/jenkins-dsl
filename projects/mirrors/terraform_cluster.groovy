freeStyleJob('mirror_terraform_cluster') {
    displayName('mirror-terraform-cluster')
    description('Mirror github.com/jessfraz/terraform-cluster to g.j3ss.co/terraform-cluster.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/terraform-cluster')
        sidebarLinks {
            link('https://git.j3ss.co/terraform-cluster', 'git.j3ss.co/terraform-cluster', 'notepad.png')
        }
    }
    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }
    triggers {
        cron('H H * * *')
    }
    wrappers { colorizeOutput() }
    steps {
        shell('git clone --mirror https://github.com/jessfraz/terraform-cluster.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/terraform-cluster.git')
    }
    publishers {
        extendedEmail {
            recipientList('$DEFAULT_RECIPIENTS')
            contentType('text/plain')
            triggers {
                stillFailing {
                    attachBuildLog(true)
                }
            }
        }
        wsCleanup()
    }
}
