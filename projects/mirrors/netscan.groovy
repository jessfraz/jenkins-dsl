freeStyleJob('mirror_netscan') {
    displayName('mirror-netscan')
    description('Mirror github.com/jessfraz/netscan to g.j3ss.co/netscan.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/netscan')
        sidebarLinks {
            link('https://git.j3ss.co/netscan', 'git.j3ss.co/netscan', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/netscan.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/netscan.git')
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
