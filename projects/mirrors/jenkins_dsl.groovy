freeStyleJob('mirror_jenkins_dsl') {
    displayName('mirror-jenkins-dsl')
    description('Mirror github.com/jessfraz/jenkins-dsl to g.j3ss.co/jenkins-dsl.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/jenkins-dsl')
        sidebarLinks {
            link('https://git.j3ss.co/jenkins-dsl', 'git.j3ss.co/jenkins-dsl', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git clone --mirror git@github.com:jessfraz/jenkins-dsl.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/jenkins-dsl.git')
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
