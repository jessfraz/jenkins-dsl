freeStyleJob('reg') {
    displayName('reg')
    description('Build Dockerfiles in genuinetools/reg.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/reg')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/reg', 'Docker Hub: jess/reg', 'notepad.png')
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
        shell('export BRANCH=$(git symbolic-ref -q --short HEAD || git describe --tags --exact-match || echo "master"); if [[ "$BRANCH" == "master" ]]; then export BRANCH="latest"; fi; echo "$BRANCH" > .branch')
        shell('docker build --rm --force-rm -t r.j3ss.co/reg:$(cat .branch) .')
shell('docker tag r.j3ss.co/reg:$(cat .branch) jess/reg:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/reg:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/reg:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/reg:$(cat .branch) r.j3ss.co/reg:latest; docker push --disable-content-trust=false r.j3ss.co/reg:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/reg:$(cat .branch) jess/reg:latest; docker push --disable-content-trust=false jess/reg:latest; fi')

        shell('docker build --rm --force-rm -t r.j3ss.co/reg-server:$(cat .branch) server')
shell('docker tag r.j3ss.co/reg-server:$(cat .branch) jess/reg-server:$(cat .branch)')
shell('docker tag r.j3ss.co/reg-server:$(cat .branch) jessfraz/reg-server:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/reg-server:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/reg-server:$(cat .branch)')
        shell('docker push --disable-content-trust=false jessfraz/reg-server:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/reg-server:$(cat .branch) r.j3ss.co/reg-server:latest; docker push --disable-content-trust=false r.j3ss.co/reg-server:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/reg-server:$(cat .branch) jess/reg-server:latest; docker push --disable-content-trust=false jess/reg-server:latest; fi')
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
