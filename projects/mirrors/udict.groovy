freeStyleJob('mirror_udict') {
    displayName('mirror-udict')
    description('Mirror github.com/jessfraz/udict to g.j3ss.co/udict.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/udict')
        sidebarLinks {
            link('https://git.j3ss.co/udict', 'git.j3ss.co/udict', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/udict.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('ssh://git@g.j3ss.co:2200/~/udict.git')
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
