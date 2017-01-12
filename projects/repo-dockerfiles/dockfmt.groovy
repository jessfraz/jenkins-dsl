freeStyleJob('dockfmt') {
    displayName('dockfmt')
    description('Build Dockerfiles in jessfraz/dockfmt.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/dockfmt')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/dockfmt', 'Docker Hub: jess/dockfmt', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/dockfmt.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/dockfmt:latest .')
        shell('docker tag r.j3ss.co/dockfmt:latest jess/dockfmt:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/dockfmt:latest')
        shell('docker push --disable-content-trust=false jess/dockfmt:latest')
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
