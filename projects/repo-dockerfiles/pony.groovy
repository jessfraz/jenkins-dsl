freeStyleJob('pony') {
    displayName('pony')
    description('Build Dockerfiles in jessfraz/pony.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/pony')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/pony', 'Docker Hub: jess/pony', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/pony', 'Docker Hub: jessfraz/pony', 'notepad.png')
            link('https://r.j3ss.co/repo/pony/tags', 'Registry: r.j3ss.co/pony', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/pony.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/pony:latest .')
        shell('docker tag r.j3ss.co/pony:latest jess/pony:latest')
        shell('docker tag r.j3ss.co/pony:latest jessfraz/pony:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/pony:latest')
        shell('docker push --disable-content-trust=false jess/pony:latest')
        shell('docker push --disable-content-trust=false jessfraz/pony:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/pony:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/pony:$tag || true; done')
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
