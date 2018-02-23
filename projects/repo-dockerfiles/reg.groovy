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
        numToKeep(100)
        daysToKeep(15)
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
        cron('H H * * *')
        githubPush()
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    steps {
        shell('docker build --rm --force-rm -t r.j3ss.co/reg:latest .')
        shell('img build -t r.j3ss.co/reg:latest .')
        shell('docker tag r.j3ss.co/reg:latest jess/reg:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/reg:latest')
        shell('docker push --disable-content-trust=false jess/reg:latest')

        shell('docker build --rm --force-rm -t r.j3ss.co/reg-server:latest server')
        shell('img build -t r.j3ss.co/reg-server:latest server')
        shell('docker tag r.j3ss.co/reg-server:latest jess/reg-server:latest')
        shell('docker tag r.j3ss.co/reg-server:latest jessfraz/reg-server:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/reg-server:latest')
        shell('docker push --disable-content-trust=false jess/reg-server:latest')
        shell('docker push --disable-content-trust=false jessfraz/reg-server:latest')
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
