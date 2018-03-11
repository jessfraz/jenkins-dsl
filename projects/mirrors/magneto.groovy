freeStyleJob('mirror_magneto') {
    displayName('mirror-magneto')
    description('Mirror github.com/genuinetools/magneto to g.j3ss.co/genuinetools/magneto.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/magneto')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/magneto', 'git.j3ss.co/genuinetools/magneto', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/magneto.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/magneto.git')
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
