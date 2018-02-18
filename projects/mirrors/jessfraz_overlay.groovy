freeStyleJob('mirror_jessfraz_overlay') {
    displayName('mirror-jessfraz-overlay')
    description('Mirror github.com/jessfraz/jessfraz-overlay to g.j3ss.co/jessfraz-overlay.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/jessfraz-overlay')
        sidebarLinks {
            link('https://git.j3ss.co/jessfraz-overlay', 'git.j3ss.co/jessfraz-overlay', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/jessfraz-overlay.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/jessfraz-overlay.git')
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
