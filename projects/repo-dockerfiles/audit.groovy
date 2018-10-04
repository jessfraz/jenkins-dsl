freeStyleJob('audit') {
    displayName('audit')
    description('Build Dockerfiles in genuinetools/audit.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/audit')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/audit', 'Docker Hub: jess/audit', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/audit', 'Docker Hub: jessfraz/audit', 'notepad.png')
            link('https://r.j3ss.co/repo/audit/tags', 'Registry: r.j3ss.co/audit', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/audit.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/audit:latest .')
        shell('docker tag r.j3ss.co/audit:latest jess/audit:latest')
        shell('docker tag r.j3ss.co/audit:latest jessfraz/audit:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/audit:latest')
        shell('docker push --disable-content-trust=false jess/audit:latest')
        shell('docker push --disable-content-trust=false jessfraz/audit:latest')
        shell('for tag in $(git tag); do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/audit:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/audit:$tag || true; done')
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
