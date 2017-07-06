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
        numToKeep(100)
        daysToKeep(15)
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git clone --mirror https://github.com/jessfraz/random-forest-classifier.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/random-forest-classifier.git')
    }

    publishers {
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
