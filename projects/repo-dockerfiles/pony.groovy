freeStyleJob('pony') {
    displayName('pony')
    description('Build Dockerfiles in jessfraz/pony.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/pony')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/pony', 'Docker Hub: jess/pony', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/pony.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/pony:latest .')
        shell('docker tag r.j3ss.co/pony:latest jess/pony:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/pony:latest')
        shell('docker push --disable-content-trust=false jess/pony:latest')
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
