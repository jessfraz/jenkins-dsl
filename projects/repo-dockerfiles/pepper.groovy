freeStyleJob('pepper') {
    displayName('pepper')
    description('Build Dockerfiles in genuinetools/pepper.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/pepper')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/pepper', 'Docker Hub: jess/pepper', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/pepper', 'Docker Hub: jessfraz/pepper', 'notepad.png')
            link('https://r.j3ss.co/repo/pepper/tags', 'Registry: r.j3ss.co/pepper', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/pepper.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/pepper:$(cat .branch) .')
        shell('docker tag r.j3ss.co/pepper:$(cat .branch) jess/pepper:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/pepper:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/pepper:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/pepper:$(cat .branch) r.j3ss.co/pepper:latest; docker push --disable-content-trust=false r.j3ss.co/pepper:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/pepper:$(cat .branch) jess/pepper:latest; docker push --disable-content-trust=false jess/pepper:latest; fi')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/pepper:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/pepper:$tag || true; done')
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
