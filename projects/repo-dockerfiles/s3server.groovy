freeStyleJob('s3server') {
    displayName('s3server')
    description('Build Dockerfiles in jessfraz/s3server.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/s3server')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/s3server', 'Docker Hub: jess/s3server', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/s3server', 'Docker Hub: jessfraz/s3server', 'notepad.png')
            link('https://r.j3ss.co/repo/s3server/tags', 'Registry: r.j3ss.co/s3server', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/s3server.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/s3server:$(cat .branch) .')
        shell('docker tag r.j3ss.co/s3server:$(cat .branch) jess/s3server:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/s3server:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/s3server:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/s3server:$(cat .branch) r.j3ss.co/s3server:latest; docker push --disable-content-trust=false r.j3ss.co/s3server:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/s3server:$(cat .branch) jess/s3server:latest; docker push --disable-content-trust=false jess/s3server:latest; fi')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/s3server:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/s3server:$tag || true; done')
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
