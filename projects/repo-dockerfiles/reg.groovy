freeStyleJob('reg') {
    displayName('reg')
    description('Build Dockerfiles in genuinetools/reg.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/reg')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/reg', 'Docker Hub: jess/reg', 'notepad.png')
            link('https://r.j3ss.co/reg', 'Registry: r.j3ss.co/reg', 'notepad.png')
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
        shell('export BRANCH=$(git symbolic-ref -q --short HEAD || git describe --tags --exact-match)')
        shell('if [[ "$BRANCH" == "master" ]]; then export BRANCH="latest"; endif')
        shell('docker build --rm --force-rm -t r.j3ss.co/reg:${BRANCH} .')
        shell('docker tag r.j3ss.co/reg:${BRANCH} jess/reg:${BRANCH}')
        shell('docker push --disable-content-trust=false r.j3ss.co/reg:${BRANCH}')
        shell('docker push --disable-content-trust=false jess/reg:${BRANCH}')

        shell('docker build --rm --force-rm -t r.j3ss.co/reg-server:${BRANCH} server')
        shell('docker tag r.j3ss.co/reg-server:${BRANCH} jess/reg-server:${BRANCH}')
        shell('docker tag r.j3ss.co/reg-server:${BRANCH} jessfraz/reg-server:${BRANCH}')
        shell('docker push --disable-content-trust=false r.j3ss.co/reg-server:${BRANCH}')
        shell('docker push --disable-content-trust=false jess/reg-server:${BRANCH}')
        shell('docker push --disable-content-trust=false jessfraz/reg-server:${BRANCH}')
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
