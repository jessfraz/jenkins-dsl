freeStyleJob('mirror_releases') {
    displayName('mirror-releases')
    description('Mirror github.com/genuinetools/releases to g.j3ss.co/genuinetools/releases.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/releases')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/releases', 'git.j3ss.co/genuinetools/releases', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/releases.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/releases.git')
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
