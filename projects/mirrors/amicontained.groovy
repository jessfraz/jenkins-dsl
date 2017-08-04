freeStyleJob('mirror_amicontained') {
    displayName('mirror-amicontained')
    description('Mirror github.com/jessfraz/amicontained to g.j3ss.co/amicontained.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/amicontained')
        sidebarLinks {
            link('https://git.j3ss.co/amicontained', 'git.j3ss.co/amicontained', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/amicontained.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/amicontained.git')
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
