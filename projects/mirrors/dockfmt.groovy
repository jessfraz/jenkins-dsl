freeStyleJob('mirror_dockfmt') {
    displayName('mirror-dockfmt')
    description('Mirror github.com/jessfraz/dockfmt to g.j3ss.co/dockfmt.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/dockfmt')
        sidebarLinks {
            link('https://git.j3ss.co/dockfmt', 'git.j3ss.co/dockfmt', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/dockfmt.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/dockfmt.git')
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
