freeStyleJob('mirror_junk') {
    displayName('mirror-junk')
    description('Mirror github.com/jessfraz/junk to g.j3ss.co/junk.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/junk')
        sidebarLinks {
            link('https://git.j3ss.co/junk', 'git.j3ss.co/junk', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/junk.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/junk.git')
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
