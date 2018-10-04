freeStyleJob('battery') {
    displayName('battery')
    description('Build Dockerfiles in jessfraz/battery.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/battery')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/battery', 'Docker Hub: jess/battery', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/battery', 'Docker Hub: jessfraz/battery', 'notepad.png')
            link('https://r.j3ss.co/repo/battery/tags', 'Registry: r.j3ss.co/battery', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/battery.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/battery:latest .')
        shell('docker tag r.j3ss.co/battery:latest jess/battery:latest')
        shell('docker tag r.j3ss.co/battery:latest jessfraz/battery:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/battery:latest')
        shell('docker push --disable-content-trust=false jess/battery:latest')
        shell('docker push --disable-content-trust=false jessfraz/battery:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/battery:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/battery:$tag || true; done')
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
