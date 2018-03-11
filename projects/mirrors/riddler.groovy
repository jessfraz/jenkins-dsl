freeStyleJob('mirror_riddler') {
    displayName('mirror-riddler')
    description('Mirror github.com/genuinetools/riddler to g.j3ss.co/genuinetools/riddler.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/riddler')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/riddler', 'git.j3ss.co/genuinetools/riddler', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/riddler.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/riddler.git')
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
