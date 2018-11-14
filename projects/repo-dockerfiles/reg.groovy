freeStyleJob('reg') {
    displayName('reg')
    description('Build Dockerfiles in genuinetools/reg.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/reg')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/reg', 'Docker Hub: jess/reg', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/reg', 'Docker Hub: jessfraz/reg', 'notepad.png')
            link('https://r.j3ss.co/repo/reg/tags', 'Registry: r.j3ss.co/reg', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/reg.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/reg:latest .')
        shell('docker tag r.j3ss.co/reg:latest jess/reg:latest')
        shell('docker tag r.j3ss.co/reg:latest jessfraz/reg:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/reg:latest')
        shell('docker push --disable-content-trust=false jess/reg:latest')
        shell('docker push --disable-content-trust=false jessfraz/reg:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/reg:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/reg:$tag || true; docker tag r.j3ss.co/reg:$tag jess/reg:$tag || true; docker push --disable-content-trust=false jess/reg:$tag || true; done')
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
