freeStyleJob('present_j3ss_co') {
    displayName('present.j3ss.co')
    description('Build Dockerfiles in jessfraz/present.j3ss.co.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/present.j3ss.co')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/present', 'Docker Hub: jess/present', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/present', 'Docker Hub: jessfraz/present', 'notepad.png')
            link('https://r.j3ss.co/repo/present/tags', 'Registry: r.j3ss.co/present', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/present.j3ss.co.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/present:latest .')
        shell('docker tag r.j3ss.co/present:latest jess/present:latest')
        shell('docker tag r.j3ss.co/present:latest jessfraz/present:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/present:latest')
        shell('docker push --disable-content-trust=false jess/present:latest')
        shell('docker push --disable-content-trust=false jessfraz/present:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/present:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/present:$tag || true; done')
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
