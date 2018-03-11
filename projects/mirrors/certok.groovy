freeStyleJob('mirror_certok') {
    displayName('mirror-certok')
    description('Mirror github.com/genuinetools/certok to g.j3ss.co/genuinetools/certok.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/certok')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/certok', 'git.j3ss.co/genuinetools/certok', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/certok.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/certok.git')
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
