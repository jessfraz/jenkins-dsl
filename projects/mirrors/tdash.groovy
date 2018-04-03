freeStyleJob('mirror_tdash') {
    displayName('mirror-tdash')
    description('Mirror github.com/jessfraz/tdash to g.j3ss.co/tdash.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/tdash')
        sidebarLinks {
            link('https://git.j3ss.co/tdash', 'git.j3ss.co/tdash', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/tdash.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/tdash.git')
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
