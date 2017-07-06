freeStyleJob('mirror_dockerfiles') {
    displayName('mirror-dockerfiles')
    description('Mirror github.com/jessfraz/dockerfiles to g.j3ss.co/dockerfiles.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/dockerfiles')
        sidebarLinks {
            link('https://git.j3ss.co/dockerfiles', 'git.j3ss.co/dockerfiles', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/dockerfiles.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/dockerfiles.git')
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
