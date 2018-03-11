freeStyleJob('mirror_bane') {
    displayName('mirror-bane')
    description('Mirror github.com/genuinetools/bane to g.j3ss.co/genuinetools/bane.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/bane')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/bane', 'git.j3ss.co/genuinetools/bane', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/bane.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/bane.git')
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
