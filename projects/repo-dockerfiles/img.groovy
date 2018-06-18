freeStyleJob('img') {
    displayName('img')
    description('Build Dockerfiles in genuinetools/img.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/img')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/img', 'Docker Hub: jess/img', 'notepad.png')
            link('https://r.j3ss.co/repo/img/tags', 'Registry: r.j3ss.co/img', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/img.git')
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

    parameters {
        gitParam('GIT_BRANCH_OR_TAG') {
            description('Git Branch or Tag')
            type('BRANCH_TAG')
            defaultValue('origin/master')
            sortMode('DESCENDING_SMART')
        }
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    steps {
        shell('export BRANCH=$(git symbolic-ref -q --short HEAD || git describe --tags --exact-match || echo "master"); if [[ "$BRANCH" == "master" ]]; then export BRANCH="latest"; fi; echo "$BRANCH" > .branch')
        shell('docker build --rm --force-rm -t r.j3ss.co/img:$(cat .branch) .')
shell('docker tag r.j3ss.co/img:$(cat .branch) jess/img:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/img:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/img:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/img:$(cat .branch) r.j3ss.co/img:latest; docker push --disable-content-trust=false r.j3ss.co/img:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/img:$(cat .branch) jess/img:latest; docker push --disable-content-trust=false jess/img:latest; fi')
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
