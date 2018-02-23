freeStyleJob('tor') {
    displayName('tor')
    description('Build all the Dockerfiles in jessfraz/tor repo and rebase add-dockerfile branch.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/tor')
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/tor.git')
                name('origin')
                credentials('ssh-github-key')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/tor:latest .')
        shell('img build --rm --force-rm -t r.j3ss.co/tor:latest .')
        shell('docker tag r.j3ss.co/tor:latest jess/tor:latest')
        shell('docker tag r.j3ss.co/tor:latest jessfraz/tor:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/tor:latest')
        shell('docker push --disable-content-trust=false jess/tor:latest')
        shell('docker push --disable-content-trust=false jessfraz/tor:latest')
        shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
        shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
    }

    publishers {
        git {
            branch('origin', 'add-dockerfile')
            forcePush()
            pushOnlyIfSuccess()
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
