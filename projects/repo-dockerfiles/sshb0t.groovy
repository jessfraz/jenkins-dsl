freeStyleJob('sshb0t') {
    displayName('sshb0t')
    description('Build Dockerfiles in jessfraz/sshb0t.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/sshb0t')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/sshb0t', 'Docker Hub: jess/sshb0t', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/sshb0t.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/sshb0t:latest .')
        shell('docker tag r.j3ss.co/sshb0t:latest jess/sshb0t:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/sshb0t:latest')
        shell('docker push --disable-content-trust=false jess/sshb0t:latest')
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
