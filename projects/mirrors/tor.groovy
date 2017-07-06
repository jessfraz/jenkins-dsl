freeStyleJob('mirror_tor') {
    displayName('mirror-tor')
    description('Mirror github.com/jessfraz/tor to g.j3ss.co/tor.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/tor')
        sidebarLinks {
            link('https://git.j3ss.co/tor', 'git.j3ss.co/tor', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/tor.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/tor.git')
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
