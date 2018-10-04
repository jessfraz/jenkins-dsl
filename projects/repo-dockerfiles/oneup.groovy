freeStyleJob('oneup') {
    displayName('1up')
    description('Build Dockerfiles in genuinetools/1up.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/1up')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/1up', 'Docker Hub: jess/1up', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/1up', 'Docker Hub: jessfraz/1up', 'notepad.png')
            link('https://r.j3ss.co/repo/1up/tags', 'Registry: r.j3ss.co/1up', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/1up.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/1up:$(cat .branch) .')
        shell('docker tag r.j3ss.co/1up:$(cat .branch) jess/1up:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/1up:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/1up:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/1up:$(cat .branch) r.j3ss.co/1up:latest; docker push --disable-content-trust=false r.j3ss.co/1up:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/1up:$(cat .branch) jess/1up:latest; docker push --disable-content-trust=false jess/1up:latest; fi')
        shell('for tag in "$(git tag)"; do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/1up:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/1up:$tag || true; done')
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
