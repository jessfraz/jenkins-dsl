freeStyleJob('maintenance_backup_trust') {
    displayName('backup-trust')
    description('Backup trust for the docker-registry.')

    weight(6)

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
        shell("echo 'Running backup of registry trust'")
        shell('docker run --rm --disable-content-trust=false --name gsutil --entrypoint gsutil -v /var/jenkins_home/.docker/trust:/mnt/disks/jenkins/.docker/trust:ro r.j3ss.co/gcloud -m cp -r /mnt/disks/jenkins/.docker/trust/private/* gs://misc.j3ss.co/backups/trust/')
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
