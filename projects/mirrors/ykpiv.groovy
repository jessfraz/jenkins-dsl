freeStyleJob('mirror_ykpiv') {
    displayName('mirror-ykpiv')
    description('Mirror github.com/jessfraz/ykpiv to g.j3ss.co/ykpiv.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/ykpiv')
        sidebarLinks {
            link('https://git.j3ss.co/ykpiv', 'git.j3ss.co/ykpiv', 'notepad.png')
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
        shell('git clone --mirror git@github.com:jessfraz/ykpiv.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/ykpiv.git')
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
