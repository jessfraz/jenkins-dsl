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
        numToKeep(100)
        daysToKeep(15)
    }
    triggers {
        cron('H H * * *')
    }
    wrappers { colorizeOutput() }
    steps {
        shell('git clone --mirror https://github.com/jessfraz/systemd-butts.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/systemd-butts.git')
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
