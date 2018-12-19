freeStyleJob('morningpaper2remarkable') {
    displayName('morningpaper2remarkable')
    description('Build Dockerfiles in jessfraz/morningpaper2remarkable.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/morningpaper2remarkable')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/morningpaper2remarkable', 'Docker Hub: jess/morningpaper2remarkable', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/morningpaper2remarkable', 'Docker Hub: jessfraz/morningpaper2remarkable', 'notepad.png')
            link('https://r.j3ss.co/repo/morningpaper2remarkable/tags', 'Registry: r.j3ss.co/morningpaper2remarkable', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/morningpaper2remarkable.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/morningpaper2remarkable:latest .')
        shell('docker tag r.j3ss.co/morningpaper2remarkable:latest jess/morningpaper2remarkable:latest')
        shell('docker tag r.j3ss.co/morningpaper2remarkable:latest jessfraz/morningpaper2remarkable:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/morningpaper2remarkable:latest')
        shell('docker push --disable-content-trust=false jess/morningpaper2remarkable:latest')
        shell('docker push --disable-content-trust=false jessfraz/morningpaper2remarkable:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/morningpaper2remarkable:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/morningpaper2remarkable:$tag || true; docker tag r.j3ss.co/morningpaper2remarkable:$tag jess/morningpaper2remarkable:$tag || true; docker push --disable-content-trust=false jess/morningpaper2remarkable:$tag || true; done')
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
