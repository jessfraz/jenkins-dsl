freeStyleJob('tdash') {
    displayName('tdash')
    description('Build Dockerfiles in jessfraz/tdash.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/tdash')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/tdash', 'Docker Hub: jess/tdash', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/tdash.git')
            }
branches('*/master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
        githubPush()
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    steps {
        shell('docker build --rm --force-rm -t r.j3ss.co/tdash:latest .')
        shell('docker tag r.j3ss.co/tdash:latest jess/tdash:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/tdash:latest')
        shell('docker push --disable-content-trust=false jess/tdash:latest')
        shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
        shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
    }

    publishers {
        retryBuild {
            retryLimit(2)
            fixedDelay(15)
        }

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
