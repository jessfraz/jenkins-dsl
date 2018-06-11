freeStyleJob('ejabberd') {
    displayName('ejabberd')
    description('Build Dockerfiles in rroemhild/docker-ejabberd.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/rroemhild/docker-ejabberd')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/ejabberd', 'Docker Hub: jess/ejabberd', 'notepad.png')
            link('https://r.j3ss.co/ejabberd', 'Registry: r.j3ss.co/ejabberd', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/rroemhild/docker-ejabberd.git')
            }
            branches('*/master', '*/tags/*')
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
        shell('export BRANCH=$(git symbolic-ref -q --short HEAD || git describe --tags --exact-match); if [[ "$BRANCH" == "master" ]]; then export BRANCH="latest"; fi; echo "$BRANCH" > .branch')
        shell('docker build --rm --force-rm -t r.j3ss.co/ejabberd:$(cat .branch) .')
        shell('docker tag r.j3ss.co/ejabberd:${BRANCH} jess/ejabberd:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/ejabberd:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/ejabberd:$(cat .branch)')
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
