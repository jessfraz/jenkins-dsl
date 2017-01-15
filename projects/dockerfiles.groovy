freeStyleJob('dockerfiles') {
    displayName('dockerfiles')
    description('Build all the Dockerfiles in jessfraz/dockerfiles repo and pushes them to r.j3ss.co.')

    blockOn('^docker_hub_dockerfiles.*')

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
        cron('H H/5 * * *')
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

    // TODO: enable this when alpine:v3.5 is signed
    environmentVariables(DOCKER_CONTENT_TRUST: '0')
    steps {
        shell('if [ ! -f /usr/bin/parallel ] ; then docker exec -u root jenkins apk add --no-cache parallel; fi')

        shell('./build-all.sh')
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
    }
}
