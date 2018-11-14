freeStyleJob('bpfps') {
    displayName('bpfps')
    description('Build Dockerfiles in genuinetools/bpfps.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/bpfps')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/bpfps', 'Docker Hub: jess/bpfps', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/bpfps', 'Docker Hub: jessfraz/bpfps', 'notepad.png')
            link('https://r.j3ss.co/repo/bpfps/tags', 'Registry: r.j3ss.co/bpfps', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/bpfps.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/bpfps:latest .')
        shell('docker tag r.j3ss.co/bpfps:latest jess/bpfps:latest')
        shell('docker tag r.j3ss.co/bpfps:latest jessfraz/bpfps:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/bpfps:latest')
        shell('docker push --disable-content-trust=false jess/bpfps:latest')
        shell('docker push --disable-content-trust=false jessfraz/bpfps:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/bpfps:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/bpfps:$tag || true; docker tag r.j3ss.co/bpfps:$tag jess/bpfps:$tag || true; docker push --disable-content-trust=false jess/bpfps:$tag || true; done')
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
