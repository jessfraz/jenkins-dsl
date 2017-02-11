freeStyleJob('update_fork_linux_next_security') {
    displayName('update-fork-linux-security')
    description('Rebase the linux-security branch in jessfraz/linux fork.')

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
                refspec('+refs/heads/linux-security:refs/remotes/origin/linux-security')
            }
            remote {
                url('git://git.kernel.org/pub/scm/linux/kernel/git/jmorris/linux-security.git')
                name('upstream')
                refspec('+refs/heads/master:refs/remotes/upstream/master')
            }
            branches('linux-security', 'upstream/master')
            extensions {
                cloneOptions {
                    shallow(true)
                }
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
        postBuildScripts {
            git {
                branch('origin', 'linux-security')
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
