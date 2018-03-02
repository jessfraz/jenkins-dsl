freeStyleJob('pastebinit') {
    displayName('pastebinit')
    description('Build Dockerfiles in jessfraz/pastebinit.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/pastebinit')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/pastebinit', 'Docker Hub: jess/pastebinit', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/pastebinit.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/pastebinit:latest .')
        shell('docker tag r.j3ss.co/pastebinit:latest jess/pastebinit:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/pastebinit:latest')
        shell('docker push --disable-content-trust=false jess/pastebinit:latest')

        shell('docker build --rm --force-rm -t r.j3ss.co/pastebinit-server:latest server')
        shell('docker tag r.j3ss.co/pastebinit-server:latest jess/pastebinit-server:latest')
        shell('docker tag r.j3ss.co/pastebinit-server:latest jessfraz/pastebinit-server:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/pastebinit-server:latest')
        shell('docker push --disable-content-trust=false jess/pastebinit-server:latest')
        shell('docker push --disable-content-trust=false jessfraz/pastebinit-server:latest')
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
