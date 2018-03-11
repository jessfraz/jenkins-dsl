freeStyleJob('mirror_amicontained') {
    displayName('mirror-amicontained')
    description('Mirror github.com/genuinetools/amicontained to g.j3ss.co/genuinetools/amicontained.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/amicontained')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/amicontained', 'git.j3ss.co/genuinetools/amicontained', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/amicontained.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/amicontained.git')
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
