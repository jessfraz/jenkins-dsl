freeStyleJob('mirror_dstats') {
    displayName('mirror-dstats')
    description('Mirror github.com/jessfraz/dstats to g.j3ss.co/dstats.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/dstats')
        sidebarLinks {
            link('https://git.j3ss.co/dstats', 'git.j3ss.co/dstats', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/dstats.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/dstats.git')
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
