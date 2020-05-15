freeStyleJob('update_fork_kali_linux_docker') {
    displayName('update-fork-kali-linux-docker')
    description('Rebase the primary branch (master) in jessfraz/kali-linux-docker fork.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/kali-linux-docker')
        sidebarLinks {
            link('https://github.com/mathiasbynens/kali-linux-docker', 'UPSTREAM: mathiasbynens/kali-linux-docker', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/kali-linux-docker.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('https://github.com/mathiasbynens/kali-linux-docker.git')
                name('upstream')
                refspec('+refs/heads/master:refs/remotes/upstream/master')
            }
            branches('master', 'upstream/master')
            extensions {
                disableRemotePoll()
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git rebase upstream/master')
    }

    publishers {
        git {
            branch('origin', 'master')
            pushOnlyIfSuccess()
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
