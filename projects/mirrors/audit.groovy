freeStyleJob('mirror_audit') {
    displayName('mirror-audit')
    description('Mirror github.com/genuinetools/audit to g.j3ss.co/genuinetools/audit.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/audit')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/audit', 'git.j3ss.co/genuinetools/audit', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/audit.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/audit.git')
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
