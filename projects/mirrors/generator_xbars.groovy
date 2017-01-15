freeStyleJob('mirror_generator_xbars') {
    displayName('mirror-generator-xbars')
    description('Mirror github.com/jessfraz/generator-xbars to g.j3ss.co/generator-xbars.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/generator-xbars')
        sidebarLinks {
            link('https://git.j3ss.co/generator-xbars', 'git.j3ss.co/generator-xbars', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/generator-xbars.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('ssh://git@g.j3ss.co:2200/~/generator-xbars.git')
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
