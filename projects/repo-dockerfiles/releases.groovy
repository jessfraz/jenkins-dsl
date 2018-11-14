freeStyleJob('releases') {
    displayName('releases')
    description('Build Dockerfiles in genuinetools/releases.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/releases')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/releases', 'Docker Hub: jess/releases', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/releases', 'Docker Hub: jessfraz/releases', 'notepad.png')
            link('https://r.j3ss.co/repo/releases/tags', 'Registry: r.j3ss.co/releases', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/releases.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/releases:latest .')
        shell('docker tag r.j3ss.co/releases:latest jess/releases:latest')
        shell('docker tag r.j3ss.co/releases:latest jessfraz/releases:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/releases:latest')
        shell('docker push --disable-content-trust=false jess/releases:latest')
        shell('docker push --disable-content-trust=false jessfraz/releases:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/releases:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/releases:$tag || true; docker tag r.j3ss.co/releases:$tag jess/releases:$tag || true; docker push --disable-content-trust=false jess/releases:$tag || true; done')
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
