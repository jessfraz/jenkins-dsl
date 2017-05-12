freeStyleJob('dockerfiles') {
    displayName('dockerfiles')
    description('Build all the Dockerfiles in jessfraz/dockerfiles repo and pushes them to r.j3ss.co.')

    weight(4)

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/dockerfiles')
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote { url('https://github.com/jessfraz/dockerfiles.git') }
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

    wrappers {
        colorizeOutput()

        // timeout if there has been no activity for 180 seconds
        // then fail the build and set a build description
        timeout {
            noActivity(3600)
            failBuild()
            writeDescription('Build failed due to timeout after {0} minutes')
        }
    }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    environmentVariables(JOBS: '5')
    steps {
        shell('if [ ! -f /usr/bin/parallel ] ; then docker exec -u root jenkins apk add --no-cache parallel; fi')

        shell('./build-all.sh')
        shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
        shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
    }

    publishers {
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
