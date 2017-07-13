freeStyleJob('kali_linux') {
    displayName('kali-linux')
    description('Build Docker images for kali-linux.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/kali-linux-docker')
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/kali-linux-docker.git')
            }
            branches('updates')
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
        shell('docker run --rm --privileged -v $(pwd | sed \'s#/var/jenkins_home/#/mnt/disks/jenkins/#\')/build.sh:/usr/bin/build.sh:ro -v /var/run/docker.sock:/var/run/docker.sock r.j3ss.co/mesos-dev build.sh')
        shell('docker push --disable-content-trust=false r.j3ss.co/kalilinux:latest')
		shell('docker tag r.j3ss.co/kalilinux:latest jess/kalilinux:latest')
        shell('docker push --disable-content-trust=false jess/kalilinux:latest')
        shell('docker run --rm --disable-content-trust=true r.j3ss.co/kalilinux awk \'{print $NF}\' /etc/debian_version | sed \'s/\\r$//\' | tr \'[:upper:]\' \'[:lower:]\' > tag')
		shell('docker tag r.j3ss.co/kalilinux:latest r.j3ss.co/kalilinux:$(cat tag)')
        shell('docker push --disable-content-trust=false r.j3ss.co/kalilinux:$(cat tag)')
		shell('docker tag r.j3ss.co/kalilinux:latest jess/kalilinux:$(cat tag)')
        shell('docker push --disable-content-trust=false jess/kalilinux:$(cat tag)')
        shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
        shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
    }

    publishers {
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
