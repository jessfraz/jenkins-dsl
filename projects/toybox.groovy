freeStyleJob('toybox') {
    displayName('toybox')
    description('Build projects in toybox.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/toybox')
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/toybox.git')
                credentials('toybox-deploy-key')
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

    steps {
        shell('if [ ! -f /usr/bin/make ] ; then docker exec -u root jenkins apk add --no-cache make; fi')
            shell('make ci')
    }

    publishers {
        postBuildScripts {
            steps {
                shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
                shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
            }
            onlyIfBuildSucceeds(false)
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
