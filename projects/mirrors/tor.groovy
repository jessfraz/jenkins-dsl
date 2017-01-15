freeStyleJob('mirror_tor') {
    displayName('mirror-tor')
    description('Mirror github.com/jessfraz/tor to g.j3ss.co/tor.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/tor')
        sidebarLinks {
            link('https://git.j3ss.co/tor', 'git.j3ss.co/tor', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/tor.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('ssh://git@g.j3ss.co:2200/~/tor.git')
                name('mirror')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/upstream/master')
            }
            branches('master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H/5 * * *')
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
