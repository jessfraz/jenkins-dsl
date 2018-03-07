freeStyleJob('mirror_pony') {
    displayName('mirror-pony')
    description('Mirror github.com/jessfraz/pony to g.j3ss.co/pony.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/pony')
        sidebarLinks {
            link('https://git.j3ss.co/pony', 'git.j3ss.co/pony', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/pony.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/pony.git')
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
