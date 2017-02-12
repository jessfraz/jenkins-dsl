freeStyleJob('mirror_bane') {
    displayName('mirror-bane')
    description('Mirror github.com/jessfraz/bane to g.j3ss.co/bane.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/bane')
        sidebarLinks {
            link('https://git.j3ss.co/bane', 'git.j3ss.co/bane', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git clone --mirror git@github.com:jessfraz/bane.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/bane.git')
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
