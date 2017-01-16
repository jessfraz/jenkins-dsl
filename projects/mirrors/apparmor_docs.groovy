freeStyleJob('mirror_apparmor_docs') {
    displayName('mirror-apparmor-docs')
    description('Mirror github.com/jessfraz/apparmor-docs to g.j3ss.co/apparmor-docs.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/apparmor-docs')
        sidebarLinks {
            link('https://git.j3ss.co/apparmor-docs', 'git.j3ss.co/apparmor-docs', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/apparmor-docs.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('ssh://git@g.j3ss.co:2200/~/apparmor-docs.git')
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
