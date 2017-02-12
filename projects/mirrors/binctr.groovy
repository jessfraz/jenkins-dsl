freeStyleJob('mirror_binctr') {
    displayName('mirror-binctr')
    description('Mirror github.com/jessfraz/binctr to g.j3ss.co/binctr.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/binctr')
        sidebarLinks {
            link('https://git.j3ss.co/binctr', 'git.j3ss.co/binctr', 'notepad.png')
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
        shell('git clone --mirror git@github.com:jessfraz/binctr.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/binctr.git')
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
