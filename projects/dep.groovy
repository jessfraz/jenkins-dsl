freeStyleJob('dep') {
    displayName('dep')
    description('Build golang/dep.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/golang/dep')
    }

    logRotator {
        numToKeep(10)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:golang/dep.git')
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
                credentials('dep-deploy-key')
            }
            branch('${sha1}')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        githubPullRequest {
            useGitHubHooks()
            permitAll()
            extensions {
                commitStatus {
                    context('jenkins')
                    completedStatus('SUCCESS', 'Tests passed')
                    completedStatus('FAILURE', 'Tests failed, check go build, go test, and go vet')
                    completedStatus('PENDING', 'In progress...')
                    completedStatus('ERROR', 'Something went really wrong. Ask Jess!')
                    statusUrl('https://misc.j3ss.co/jenkins/dep/${BUILD_NUMBER}/log.txt')
                }
            }
        }
    }

    wrappers { colorizeOutput() }

    steps {
        shell('docker run --rm --disable-content-trust=false -w /go/src/github.com/golang/dep -v $(pwd | sed \'s#/var/jenkins_home/#/var/lib/docker/jenkins/#\'):/go/src/github.com/golang/dep golang:latest sh -c "go build && go test && go vet"')
    }

    publishers {
        postBuildScripts {
            steps {
                shell('docker run --rm --disable-content-trust=false --name gsutil -v /var/lib/docker/jenkins/jobs/dep/builds/${BUILD_NUMBER}/log:/build_log -v /home/jessfraz/.gsutil:/root/.gsutil -v /home/jessfraz/.gcloud:/root/.config/gcloud --entrypoint sh r.j3ss.co/gcloud -c "cp /build_log /tmp/build_log.txt && gsutil cp -a public-read /tmp/build_log.txt gs://misc.j3ss.co/jenkins/dep/${BUILD_NUMBER}/log.txt"')
            }
            onlyIfBuildSucceeds(false)
        }
    }
}
