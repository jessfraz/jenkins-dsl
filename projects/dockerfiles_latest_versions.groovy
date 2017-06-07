freeStyleJob('dockerfiles_latest_versions') {
    displayName('dockerfiles-latest-versions')
    description('Version check for all the Dockerfiles in jessfraz/dockerfiles repo.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/dockerfiles')
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote { url('https://github.com/jessfraz/dockerfiles.git') }
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

    wrappers {
        colorizeOutput()

        credentialsBinding {
            string('GITHUB_TOKEN', 'github-token')
        }

        // timeout if there has been no activity for 180 seconds
        // then fail the build and set a build description
        timeout {
            noActivity(3600)
            failBuild()
            writeDescription('Build failed due to timeout after {0} minutes')
        }
    }

    steps {
        shell('if [ ! -f /usr/bin/jq ] ; then docker exec -u root jenkins apk add --no-cache jq; fi')

        shell('./latest-versions.sh')
    }

    publishers {
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
