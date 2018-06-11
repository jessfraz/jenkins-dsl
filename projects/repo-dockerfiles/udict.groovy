freeStyleJob('udict') {
    displayName('udict')
    description('Build Dockerfiles in genuinetools/udict.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/udict')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/udict', 'Docker Hub: jess/udict', 'notepad.png')
            link('https://r.j3ss.co/repo/udict/tags', 'Registry: r.j3ss.co/udict', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/udict.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/udict:$(cat .branch) .')
shell('docker tag r.j3ss.co/udict:$(cat .branch) jess/udict:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/udict:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/udict:$(cat .branch)')
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
