freeStyleJob('mirror_irssi') {
    displayName('mirror-irssi')
    description('Mirror github.com/jessfraz/irssi to g.j3ss.co/irssi.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/irssi')
        sidebarLinks {
            link('https://git.j3ss.co/irssi', 'git.j3ss.co/irssi', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/irssi.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/irssi.git')
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
