freeStyleJob('mirror_pastebinit') {
    displayName('mirror-pastebinit')
    description('Mirror github.com/jessfraz/pastebinit to g.j3ss.co/pastebinit.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/pastebinit')
        sidebarLinks {
            link('https://git.j3ss.co/pastebinit', 'git.j3ss.co/pastebinit', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/pastebinit.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/pastebinit.git')
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
