freeStyleJob('mirror_audit') {
    displayName('mirror-audit')
    description('Mirror github.com/jessfraz/audit to g.j3ss.co/audit.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/audit')
        sidebarLinks {
            link('https://git.j3ss.co/audit', 'git.j3ss.co/audit', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/audit.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/audit.git')
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
