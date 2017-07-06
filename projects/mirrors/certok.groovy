freeStyleJob('mirror_certok') {
    displayName('mirror-certok')
    description('Mirror github.com/jessfraz/certok to g.j3ss.co/certok.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/certok')
        sidebarLinks {
            link('https://git.j3ss.co/certok', 'git.j3ss.co/certok', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/certok.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/certok.git')
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
