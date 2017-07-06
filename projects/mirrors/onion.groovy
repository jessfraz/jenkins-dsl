freeStyleJob('mirror_onion') {
    displayName('mirror-onion')
    description('Mirror github.com/jessfraz/onion to g.j3ss.co/onion.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/onion')
        sidebarLinks {
            link('https://git.j3ss.co/onion', 'git.j3ss.co/onion', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/onion.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/onion.git')
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
