freeStyleJob('snippetlib') {
    displayName('snippetlib')
    description('Build Dockerfiles in jessfraz/snippetlib.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/snippetlib')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/snippetlib', 'Docker Hub: jess/snippetlib', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/snippetlib', 'Docker Hub: jessfraz/snippetlib', 'notepad.png')
            link('https://r.j3ss.co/repo/snippetlib/tags', 'Registry: r.j3ss.co/snippetlib', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/snippetlib.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/snippetlib:latest .')
        shell('docker tag r.j3ss.co/snippetlib:latest jess/snippetlib:latest')
        shell('docker tag r.j3ss.co/snippetlib:latest jessfraz/snippetlib:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/snippetlib:latest')
        shell('docker push --disable-content-trust=false jess/snippetlib:latest')
        shell('docker push --disable-content-trust=false jessfraz/snippetlib:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/snippetlib:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/snippetlib:$tag || true; docker tag r.j3ss.co/snippetlib:$tag jess/snippetlib:$tag || true; docker push --disable-content-trust=false jess/snippetlib:$tag || true; done')
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
