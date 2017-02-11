freeStyleJob('udict') {
    displayName('udict')
    description('Build Dockerfiles in jessfraz/udict.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/udict')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/udict', 'Docker Hub: jess/udict', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/udict.git')
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

    environmentVariables(DOCKER_CONTENT_TRUST: '0')
    steps {
        shell('docker build --rm --force-rm -t r.j3ss.co/udict:latest .')
        shell('docker tag r.j3ss.co/udict:latest jess/udict:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/udict:latest')
        shell('docker push --disable-content-trust=false jess/udict:latest')
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
