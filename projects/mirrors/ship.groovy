freeStyleJob('mirror_ship') {
    displayName('mirror-ship')
    description('Mirror github.com/jessfraz/ship to g.j3ss.co/ship.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/ship')
        sidebarLinks {
            link('https://git.j3ss.co/ship', 'git.j3ss.co/ship', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/ship.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/ship.git')
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
