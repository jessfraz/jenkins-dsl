freeStyleJob('update_fork_runtime') {
    displayName('update-fork-runtime')
    description('Rebase the primary branch (null) in jessfraz/runtime fork.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/runtime')
        sidebarLinks {
            link('https://github.com/null/runtime', 'UPSTREAM: null/runtime', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/runtime.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/null:refs/remotes/origin/null')
            }
            remote {
                url('https://github.com/null/runtime.git')
                name('upstream')
                refspec('+refs/heads/null:refs/remotes/upstream/null')
            }
            branches('null', 'upstream/null')
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
        shell('git rebase upstream/null')
    }

    publishers {
        git {
            branch('origin', 'null')
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
