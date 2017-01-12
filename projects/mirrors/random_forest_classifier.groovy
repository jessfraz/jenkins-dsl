freeStyleJob('mirror_random_forest_classifier') {
    displayName('mirror-random-forest-classifier')
    description('Mirror github.com/jessfraz/random-forest-classifier to g.j3ss.co/random-forest-classifier.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/random-forest-classifier')
        sidebarLinks {
            link('https://git.j3ss.co/random-forest-classifier', 'git.j3ss.co/random-forest-classifier', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/random-forest-classifier.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('ssh://git@g.j3ss.co:2200/~/random-forest-classifier.git')
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
