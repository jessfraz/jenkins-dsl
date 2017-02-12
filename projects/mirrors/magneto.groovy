freeStyleJob('mirror_magneto') {
    displayName('mirror-magneto')
    description('Mirror github.com/jessfraz/magneto to g.j3ss.co/magneto.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/magneto')
        sidebarLinks {
            link('https://git.j3ss.co/magneto', 'git.j3ss.co/magneto', 'notepad.png')
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
        shell('git clone --mirror git@github.com:jessfraz/magneto.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/magneto.git')
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
