freeStyleJob('mirror_www') {
    displayName('mirror-www')
    description('Mirror github.com/genuinetools/www to g.j3ss.co/genuinetools/www.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/www')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/www', 'git.j3ss.co/genuinetools/www', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/www.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/www.git')
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
