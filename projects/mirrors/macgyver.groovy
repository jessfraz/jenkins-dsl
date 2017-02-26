freeStyleJob('mirror_macgyver') {
    displayName('mirror-macgyver')
    description('Mirror github.com/jessfraz/macgyver to g.j3ss.co/macgyver.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/macgyver')
        sidebarLinks {
            link('https://git.j3ss.co/macgyver', 'git.j3ss.co/macgyver', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git clone --mirror https://github.com/jessfraz/macgyver.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/macgyver.git')
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
