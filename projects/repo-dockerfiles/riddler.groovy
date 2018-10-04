freeStyleJob('riddler') {
    displayName('riddler')
    description('Build Dockerfiles in genuinetools/riddler.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/riddler')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/riddler', 'Docker Hub: jess/riddler', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/riddler', 'Docker Hub: jessfraz/riddler', 'notepad.png')
            link('https://r.j3ss.co/repo/riddler/tags', 'Registry: r.j3ss.co/riddler', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/riddler.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/riddler:$(cat .branch) .')
        shell('docker tag r.j3ss.co/riddler:$(cat .branch) jess/riddler:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/riddler:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/riddler:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/riddler:$(cat .branch) r.j3ss.co/riddler:latest; docker push --disable-content-trust=false r.j3ss.co/riddler:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/riddler:$(cat .branch) jess/riddler:latest; docker push --disable-content-trust=false jess/riddler:latest; fi')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/riddler:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/riddler:$tag || true; done')
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
