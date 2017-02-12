freeStyleJob('mirror_systemd_butts') {
    displayName('mirror-systemd-butts')
    description('Mirror github.com/jessfraz/systemd-butts to g.j3ss.co/systemd-butts.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/systemd-butts')
        sidebarLinks {
            link('https://git.j3ss.co/systemd-butts', 'git.j3ss.co/systemd-butts', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/systemd-butts.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('ssh://git@g.j3ss.co:2200/~/systemd-butts.git')
                name('mirror')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/upstream/master')
            }
            branches('master')
            extensions {
                ignoreNotifyCommit()
                disableRemotePoll()

                submoduleOptions {
                    recursive()
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

    publishers {
        postBuildScripts {
            git {
                branch('mirror', 'master')
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
