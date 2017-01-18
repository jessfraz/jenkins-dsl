freeStyleJob('tor') {
    displayName('tor')
    description('Build all the Dockerfiles in jessfraz/tor repo and rebase add-dockerfile branch.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/tor')
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/tor.git')
                name('origin')
                credentials('tor-deploy-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('https://git.torproject.org/tor.git')
                name('upstream')
                refspec('+refs/heads/master:refs/remotes/upstream/master')
            }
            branches('master', 'upstream/master', 'origin/add-dockerfile')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    steps {
        shell('git checkout origin/add-dockerfile -b add-dockerfile')
        shell('git rebase upstream/master')
        shell('docker build --rm --force-rm -t jess/tor:latest .')
        shell('docker tag jess/tor:latest r.j3ss.co/tor:latest')
        shell('docker push --disable-content-trust=false jess/tor:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/tor:latest')
    }

    publishers {
        postBuildScripts {
            git {
                branch('origin', 'add-dockerfile')
                forcePush()
                pushOnlyIfSuccess()
            }

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
