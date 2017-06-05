freeStyleJob('configs') {
    displayName('configs')
    description('Run linters and tests for configs repository.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/configs')
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/configs.git')
                credentials('configs-deploy-key')
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
        shell('docker run --rm -it -v $(pwd | sed \'s#/var/jenkins_home/#/mnt/disks/jenkins/#\')/nginx:/etc/nginx:ro r.j3ss.co/gixy /etc/nginx/nginx.conf')
        shell('docker run --rm -it -v $(pwd | sed \'s#/var/jenkins_home/#/mnt/disks/jenkins/#\')/telize/nginx.conf:/etc/nginx/nginx.conf:ro -v $(pwd | sed \'s#/var/jenkins_home/#/mnt/disks/jenkins/#\')/telize/telize.conf:/etc/nginx/conf.d/telize.conf:ro r.j3ss.co/gixy /etc/nginx/nginx.conf')
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
