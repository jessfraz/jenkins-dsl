freeStyleJob('mirror_upmail') {
    displayName('mirror-upmail')
    description('Mirror github.com/jessfraz/upmail to g.j3ss.co/upmail.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/upmail')
        sidebarLinks {
            link('https://git.j3ss.co/upmail', 'git.j3ss.co/upmail', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/upmail.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/upmail.git')
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
