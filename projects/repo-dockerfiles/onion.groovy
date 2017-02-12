freeStyleJob('onion') {
    displayName('onion')
    description('Build Dockerfiles in jessfraz/onion.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/onion')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/onion', 'Docker Hub: jess/onion', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/onion.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/onion:latest .')
        shell('docker tag r.j3ss.co/onion:latest jess/onion:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/onion:latest')
        shell('docker push --disable-content-trust=false jess/onion:latest')
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
