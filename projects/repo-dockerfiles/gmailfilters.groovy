freeStyleJob('gmailfilters') {
    displayName('gmailfilters')
    description('Build Dockerfiles in jessfraz/gmailfilters.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/gmailfilters')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/gmailfilters', 'Docker Hub: jess/gmailfilters', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/gmailfilters', 'Docker Hub: jessfraz/gmailfilters', 'notepad.png')
            link('https://r.j3ss.co/repo/gmailfilters/tags', 'Registry: r.j3ss.co/gmailfilters', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/gmailfilters.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/gmailfilters:latest .')
        shell('docker tag r.j3ss.co/gmailfilters:latest jess/gmailfilters:latest')
        shell('docker tag r.j3ss.co/gmailfilters:latest jessfraz/gmailfilters:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/gmailfilters:latest')
        shell('docker push --disable-content-trust=false jess/gmailfilters:latest')
        shell('docker push --disable-content-trust=false jessfraz/gmailfilters:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/gmailfilters:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/gmailfilters:$tag || true; docker tag r.j3ss.co/gmailfilters:$tag jess/gmailfilters:$tag || true; docker push --disable-content-trust=false jess/gmailfilters:$tag || true; done')
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
