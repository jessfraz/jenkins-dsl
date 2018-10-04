freeStyleJob('bpfd') {
    displayName('bpfd')
    description('Build Dockerfiles in genuinetools/bpfd.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/bpfd')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/bpfd', 'Docker Hub: jess/bpfd', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/bpfd', 'Docker Hub: jessfraz/bpfd', 'notepad.png')
            link('https://r.j3ss.co/repo/bpfd/tags', 'Registry: r.j3ss.co/bpfd', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/bpfd.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/bpfd:latest .')
        shell('docker tag r.j3ss.co/bpfd:latest jess/bpfd:latest')
        shell('docker tag r.j3ss.co/bpfd:latest jessfraz/bpfd:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/bpfd:latest')
        shell('docker push --disable-content-trust=false jess/bpfd:latest')
        shell('docker push --disable-content-trust=false jessfraz/bpfd:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/bpfd:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/bpfd:$tag || true; done')
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
