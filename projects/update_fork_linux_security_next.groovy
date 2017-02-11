freeStyleJob('update_fork_linux_security_next') {
    displayName('update-fork-linux-security-next')
    description('Rebase the linux-security-next branch in jessfraz/linux fork.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/linux')
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/linux.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/linux-security-next:refs/remotes/origin/linux-security-next')
            }
            remote {
                url('git://git.kernel.org/pub/scm/linux/kernel/git/jmorris/linux-security.git')
                name('upstream')
                refspec('+refs/heads/next:refs/remotes/upstream/next')
            }
            branches('linux-security-next', 'upstream/next')
            extensions {
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
        shell('git rebase upstream/next')
    }

    publishers {
        postBuildScripts {
            git {
                branch('origin', 'linux-security-next')
                pushOnlyIfSuccess()
            }
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
