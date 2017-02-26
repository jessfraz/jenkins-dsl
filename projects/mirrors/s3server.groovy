freeStyleJob('mirror_s3server') {
    displayName('mirror-s3server')
    description('Mirror github.com/jessfraz/s3server to g.j3ss.co/s3server.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/s3server')
        sidebarLinks {
            link('https://git.j3ss.co/s3server', 'git.j3ss.co/s3server', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/s3server.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/s3server.git')
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
