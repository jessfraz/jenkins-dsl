freeStyleJob('mirror_gmailfilters') {
    displayName('mirror-gmailfilters')
    description('Mirror github.com/jessfraz/gmailfilters to g.j3ss.co/gmailfilters.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/gmailfilters')
        sidebarLinks {
            link('https://git.j3ss.co/gmailfilters', 'git.j3ss.co/gmailfilters', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/gmailfilters.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/gmailfilters.git')
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
