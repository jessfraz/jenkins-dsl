freeStyleJob('sshb0t') {
    displayName('sshb0t')
    description('Build Dockerfiles in genuinetools/sshb0t.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/sshb0t')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/sshb0t', 'Docker Hub: jess/sshb0t', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/sshb0t', 'Docker Hub: jessfraz/sshb0t', 'notepad.png')
            link('https://r.j3ss.co/repo/sshb0t/tags', 'Registry: r.j3ss.co/sshb0t', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/sshb0t.git')
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
        shell('echo latest > .branch')
        shell('docker build --rm --force-rm -t r.j3ss.co/sshb0t:$(cat .branch) .')
        shell('docker tag r.j3ss.co/sshb0t:$(cat .branch) jess/sshb0t:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/sshb0t:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/sshb0t:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/sshb0t:$(cat .branch) r.j3ss.co/sshb0t:latest; docker push --disable-content-trust=false r.j3ss.co/sshb0t:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/sshb0t:$(cat .branch) jess/sshb0t:latest; docker push --disable-content-trust=false jess/sshb0t:latest; fi')
        shell('for tag in "$(git tag)"; do git checkout $tag; docker build  --rm --force-rm -t r.j3ss.co/sshb0t:$tag . || true; docker push --disable-content-trust=false r.j3ss.co/sshb0t:$tag || true; done')
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
