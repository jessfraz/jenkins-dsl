freeStyleJob('mirror_oneup') {
    displayName('mirror-1up')
    description('Mirror github.com/genuinetools/1up to g.j3ss.co/genuinetools/1up.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/1up')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/1up', 'git.j3ss.co/genuinetools/1up', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/1up.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/1up.git')
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
