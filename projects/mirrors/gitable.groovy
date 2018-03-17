freeStyleJob('mirror_gitable') {
    displayName('mirror-gitable')
    description('Mirror github.com/jessfraz/gitable to g.j3ss.co/gitable.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/gitable')
        sidebarLinks {
            link('https://git.j3ss.co/gitable', 'git.j3ss.co/gitable', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/gitable.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/gitable.git')
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
