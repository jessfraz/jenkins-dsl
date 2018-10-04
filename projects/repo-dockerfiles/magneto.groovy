freeStyleJob('magneto') {
    displayName('magneto')
    description('Build Dockerfiles in genuinetools/magneto.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/magneto')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/magneto', 'Docker Hub: jess/magneto', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/magneto', 'Docker Hub: jessfraz/magneto', 'notepad.png')
            link('https://r.j3ss.co/repo/magneto/tags', 'Registry: r.j3ss.co/magneto', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/magneto.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/magneto:latest .')
        shell('docker tag r.j3ss.co/magneto:latest jess/magneto:latest')
        shell('docker tag r.j3ss.co/magneto:latest jessfraz/magneto:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/magneto:latest')
        shell('docker push --disable-content-trust=false jess/magneto:latest')
        shell('docker push --disable-content-trust=false jessfraz/magneto:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/magneto:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/magneto:$tag || true; done')
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
