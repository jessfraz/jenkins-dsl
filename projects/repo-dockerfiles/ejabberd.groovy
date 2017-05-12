freeStyleJob('ejabberd') {
    displayName('ejabberd')
    description('Build Dockerfiles in rroemhild/docker-ejabberd.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/rroemhild/docker-ejabberd')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/ejabberd', 'Docker Hub: jess/ejabberd', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('https://github.com/rroemhild/docker-ejabberd.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/ejabberd:latest .')
        shell('docker tag r.j3ss.co/ejabberd:latest jess/ejabberd:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/ejabberd:latest')
        shell('docker push --disable-content-trust=false jess/ejabberd:latest')
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
