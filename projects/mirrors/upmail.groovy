freeStyleJob('mirror_upmail') {
    displayName('mirror-upmail')
    description('Mirror github.com/genuinetools/upmail to g.j3ss.co/genuinetools/upmail.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/upmail')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/upmail', 'git.j3ss.co/genuinetools/upmail', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/upmail.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/upmail.git')
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
