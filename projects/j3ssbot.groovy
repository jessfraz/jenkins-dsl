freeStyleJob('j3ssb0t') {
    displayName('j3ssb0t')
    description('Build Dockerfiles for j3ssb0t.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/j3ssb0t')
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/j3ssb0t.git')
                credentials('j3ssb0t-deploy-key')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/j3ssb0t:latest .')
        shell('img build --rm --force-rm -t r.j3ss.co/j3ssb0t:latest .')
        shell('docker push --disable-content-trust=false r.j3ss.co/j3ssb0t:latest')
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
