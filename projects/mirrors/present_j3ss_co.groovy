freeStyleJob('mirror_present_j3ss_co') {
    displayName('mirror-present.j3ss.co')
    description('Mirror github.com/jessfraz/present.j3ss.co to g.j3ss.co/present.j3ss.co.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/present.j3ss.co')
        sidebarLinks {
            link('https://git.j3ss.co/present.j3ss.co', 'git.j3ss.co/present.j3ss.co', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/present.j3ss.co.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('ssh://git@g.j3ss.co:2200/~/present.j3ss.co.git')
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
    }
}
