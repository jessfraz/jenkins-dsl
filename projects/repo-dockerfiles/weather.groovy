freeStyleJob('weather') {
    displayName('weather')
    description('Build Dockerfiles in genuinetools/weather.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/weather')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/weather', 'Docker Hub: jess/weather', 'notepad.png')
            link('https://r.j3ss.co/repo/weather/tags', 'Registry: r.j3ss.co/weather', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/weather.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/weather:$(cat .branch) .')
shell('docker tag r.j3ss.co/weather:$(cat .branch) jess/weather:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/weather:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/weather:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/weather:$(cat .branch) r.j3ss.co/weather:latest; docker push --disable-content-trust=false r.j3ss.co/weather:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/weather:$(cat .branch) jess/weather:latest; docker push --disable-content-trust=false jess/weather:latest; fi')

        shell('docker build --rm --force-rm -t r.j3ss.co/weather-server:$(cat .branch) server')
shell('docker tag r.j3ss.co/weather-server:$(cat .branch) jess/weather-server:$(cat .branch)')
shell('docker tag r.j3ss.co/weather-server:$(cat .branch) jessfraz/weather-server:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/weather-server:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/weather-server:$(cat .branch)')
        shell('docker push --disable-content-trust=false jessfraz/weather-server:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/weather-server:$(cat .branch) r.j3ss.co/weather-server:latest; docker push --disable-content-trust=false r.j3ss.co/weather-server:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/weather-server:$(cat .branch) jess/weather-server:latest; docker push --disable-content-trust=false jess/weather-server:latest; fi')
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
