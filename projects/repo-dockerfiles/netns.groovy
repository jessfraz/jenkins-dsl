freeStyleJob('netns') {
    displayName('netns')
    description('Build Dockerfiles in genuinetools/netns.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/netns')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/netns', 'Docker Hub: jess/netns', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/netns', 'Docker Hub: jessfraz/netns', 'notepad.png')
            link('https://r.j3ss.co/repo/netns/tags', 'Registry: r.j3ss.co/netns', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/netns.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/netns:$(cat .branch) .')
        shell('docker tag r.j3ss.co/netns:$(cat .branch) jess/netns:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/netns:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/netns:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/netns:$(cat .branch) r.j3ss.co/netns:latest; docker push --disable-content-trust=false r.j3ss.co/netns:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/netns:$(cat .branch) jess/netns:latest; docker push --disable-content-trust=false jess/netns:latest; fi')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/netns:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/netns:$tag || true; done')
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
