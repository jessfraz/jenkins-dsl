freeStyleJob('dstats') {
    displayName('dstats')
    description('Build Dockerfiles in jessfraz/dstats.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/dstats')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/dstats', 'Docker Hub: jess/dstats', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/dstats.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/dstats:latest .')
        shell('img build -t r.j3ss.co/dstats:latest .')
        shell('docker tag r.j3ss.co/dstats:latest jess/dstats:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/dstats:latest')
        shell('docker push --disable-content-trust=false jess/dstats:latest')
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
