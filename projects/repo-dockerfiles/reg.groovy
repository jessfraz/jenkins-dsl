freeStyleJob('reg') {
    displayName('reg')
    description('Build Dockerfiles in jessfraz/reg.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/reg')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/reg', 'Docker Hub: jess/reg', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/reg.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/reg:latest .')
        shell('docker tag r.j3ss.co/reg:latest jess/reg:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/reg:latest')
        shell('docker push --disable-content-trust=false jess/reg:latest')

        shell('docker build --rm --force-rm --no-cache -t r.j3ss.co/reg-server:latest server')
        shell('docker tag r.j3ss.co/reg-server:latest jess/reg-server:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/reg-server:latest')
        shell('docker push --disable-content-trust=false jess/reg-server:latest')
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
