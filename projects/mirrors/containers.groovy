freeStyleJob('mirror_containers') {
    displayName('mirror-containers')
    description('Mirror github.com/jessfraz/containers to g.j3ss.co/containers.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/containers')
        sidebarLinks {
            link('https://git.j3ss.co/containers', 'git.j3ss.co/containers', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/containers.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/containers.git')
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
