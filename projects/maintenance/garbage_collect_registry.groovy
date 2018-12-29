freeStyleJob('maintenance_garbage_collect_registry') {
    displayName('garbage-collect-registry')
    description('Run garbage collection on the docker-registry.')

    weight(4)

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    triggers {
        cron('H H * * *')
    }

    wrappers {
        colorizeOutput()

        // timeout if there has been no activity for 180 seconds
        // then fail the build and set a build description
        timeout {
            absolute(minutes = 3600)
            noActivity(36000)
            failBuild()
            writeDescription('Build failed due to timeout after {0} minutes')
        }
    }

    steps {
        shell("echo 'Running garbage collection'")
        shell('docker run --rm --disable-content-trust=false --name registry-garbage-collect -v /bin/registry:/bin/registry:ro -v /registry.yaml:/etc/docker/registry/config.yml:ro -v /registry-creds.json:/registry-creds.json:ro registry garbage-collect --delete-untagged /etc/docker/registry/config.yml')
        shell("echo 'Getting new bucket size'")
        shell('docker run --rm --disable-content-trust=false --name gsutil --entrypoint gsutil r.j3ss.co/gcloud du -s -h gs://r.j3ss.co')
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
