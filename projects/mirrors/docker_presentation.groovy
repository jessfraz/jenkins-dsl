freeStyleJob('mirror_docker_presentation') {
    displayName('mirror-docker-presentation')
    description('Mirror github.com/jessfraz/docker-presentation to g.j3ss.co/docker-presentation.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/docker-presentation')
        sidebarLinks {
            link('https://git.j3ss.co/docker-presentation', 'git.j3ss.co/docker-presentation', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/docker-presentation.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/docker-presentation.git')
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
