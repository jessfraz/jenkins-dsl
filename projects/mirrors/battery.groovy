freeStyleJob('mirror_battery') {
    displayName('mirror-battery')
    description('Mirror github.com/jessfraz/battery to g.j3ss.co/battery.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/battery')
        sidebarLinks {
            link('https://git.j3ss.co/battery', 'git.j3ss.co/battery', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/battery.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/battery.git')
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
