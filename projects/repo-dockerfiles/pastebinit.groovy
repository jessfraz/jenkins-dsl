freeStyleJob('pastebinit') {
    displayName('pastebinit')
    description('Build Dockerfiles in jessfraz/pastebinit.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/pastebinit')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/pastebinit', 'Docker Hub: jess/pastebinit', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/pastebinit', 'Docker Hub: jessfraz/pastebinit', 'notepad.png')
            link('https://r.j3ss.co/repo/pastebinit/tags', 'Registry: r.j3ss.co/pastebinit', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/pastebinit.git')
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
        shell('echo latest > .branch')
        shell('docker build --rm --force-rm -t r.j3ss.co/pastebinit:$(cat .branch) .')
        shell('docker tag r.j3ss.co/pastebinit:$(cat .branch) jess/pastebinit:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/pastebinit:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/pastebinit:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/pastebinit:$(cat .branch) r.j3ss.co/pastebinit:latest; docker push --disable-content-trust=false r.j3ss.co/pastebinit:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/pastebinit:$(cat .branch) jess/pastebinit:latest; docker push --disable-content-trust=false jess/pastebinit:latest; fi')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/pastebinit:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/pastebinit:$tag || true; done')
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
