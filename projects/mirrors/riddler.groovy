freeStyleJob('mirror_riddler') {
    displayName('mirror-riddler')
    description('Mirror github.com/jessfraz/riddler to g.j3ss.co/riddler.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/riddler')
        sidebarLinks {
            link('https://git.j3ss.co/riddler', 'git.j3ss.co/riddler', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/riddler.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/riddler.git')
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
