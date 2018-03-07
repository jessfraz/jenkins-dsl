freeStyleJob('mirror_ghb0t') {
    displayName('mirror-ghb0t')
    description('Mirror github.com/jessfraz/ghb0t to g.j3ss.co/ghb0t.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/ghb0t')
        sidebarLinks {
            link('https://git.j3ss.co/ghb0t', 'git.j3ss.co/ghb0t', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/ghb0t.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/ghb0t.git')
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
