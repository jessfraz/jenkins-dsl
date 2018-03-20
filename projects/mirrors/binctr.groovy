freeStyleJob('mirror_binctr') {
    displayName('mirror-binctr')
    description('Mirror github.com/genuinetools/binctr to g.j3ss.co/genuinetools/binctr.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/binctr')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/binctr', 'git.j3ss.co/genuinetools/binctr', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/binctr.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/binctr.git')
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
