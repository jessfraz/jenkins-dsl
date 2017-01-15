freeStyleJob('pepper') {
    displayName('pepper')
    description('Build Dockerfiles in jessfraz/pepper.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/pepper')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/pepper', 'Docker Hub: jess/pepper', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/pepper.git')
            }
branches('*/master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H/4 * * *')
        githubPush()
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    steps {
        shell('docker build --rm --force-rm -t r.j3ss.co/pepper:latest .')
        shell('docker tag r.j3ss.co/pepper:latest jess/pepper:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/pepper:latest')
        shell('docker push --disable-content-trust=false jess/pepper:latest')
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
