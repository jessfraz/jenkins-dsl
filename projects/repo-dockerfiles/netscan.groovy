freeStyleJob('netscan') {
    displayName('netscan')
    description('Build Dockerfiles in jessfraz/netscan.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/netscan')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/netscan', 'Docker Hub: jess/netscan', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/netscan', 'Docker Hub: jessfraz/netscan', 'notepad.png')
            link('https://r.j3ss.co/repo/netscan/tags', 'Registry: r.j3ss.co/netscan', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/netscan.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/netscan:$(cat .branch) .')
        shell('docker tag r.j3ss.co/netscan:$(cat .branch) jess/netscan:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/netscan:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/netscan:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/netscan:$(cat .branch) r.j3ss.co/netscan:latest; docker push --disable-content-trust=false r.j3ss.co/netscan:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/netscan:$(cat .branch) jess/netscan:latest; docker push --disable-content-trust=false jess/netscan:latest; fi')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/netscan:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/netscan:$tag || true; done')
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
