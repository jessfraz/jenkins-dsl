freeStyleJob('s3server') {
    displayName('s3server')
    description('Build Dockerfiles in jessfraz/s3server.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/s3server')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/s3server', 'Docker Hub: jess/s3server', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/s3server.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/s3server:latest .')
        shell('docker tag r.j3ss.co/s3server:latest jess/s3server:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/s3server:latest')
        shell('docker push --disable-content-trust=false jess/s3server:latest')
    }

    publishers {
    postBuildScripts {
            steps {
                shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
                shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
            }
            onlyIfBuildSucceeds(false)
        }

        retryBuild {
            retryLimit(3)
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
