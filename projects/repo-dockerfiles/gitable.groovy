freeStyleJob('gitable') {
    displayName('gitable')
    description('Build Dockerfiles in jessfraz/gitable.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/gitable')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/gitable', 'Docker Hub: jess/gitable', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/gitable', 'Docker Hub: jessfraz/gitable', 'notepad.png')
            link('https://r.j3ss.co/repo/gitable/tags', 'Registry: r.j3ss.co/gitable', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/gitable.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/gitable:latest .')
        shell('docker tag r.j3ss.co/gitable:latest jess/gitable:latest')
        shell('docker tag r.j3ss.co/gitable:latest jessfraz/gitable:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/gitable:latest')
        shell('docker push --disable-content-trust=false jess/gitable:latest')
        shell('docker push --disable-content-trust=false jessfraz/gitable:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/gitable:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/gitable:$tag || true; done')
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
