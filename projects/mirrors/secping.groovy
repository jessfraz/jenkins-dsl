freeStyleJob('mirror_secping') {
    displayName('mirror-secping')
    description('Mirror github.com/jessfraz/secping to g.j3ss.co/secping.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/secping')
        sidebarLinks {
            link('https://git.j3ss.co/secping', 'git.j3ss.co/secping', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/secping.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/secping.git')
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
