freeStyleJob('update_fork_universe') {
    displayName('update-fork-universe')
    description('Rebase the primary branch (version-3.x) in jessfraz/universe fork.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/universe')
        sidebarLinks {
            link('https://github.com/mesosphere/universe', 'UPSTREAM: mesosphere/universe', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/universe.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/version-3.x:refs/remotes/origin/version-3.x')
            }
            remote {
                url('https://github.com/mesosphere/universe.git')
                name('upstream')
                refspec('+refs/heads/version-3.x:refs/remotes/upstream/version-3.x')
            }
            branches('version-3.x', 'upstream/version-3.x')
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
        shell('git rebase upstream/version-3.x')
    }

    publishers {
        postBuildScripts {
            git {
                branch('origin', 'version-3.x')
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
