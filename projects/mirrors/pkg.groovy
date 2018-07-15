freeStyleJob('mirror_pkg') {
    displayName('mirror-pkg')
    description('Mirror github.com/genuinetools/pkg to g.j3ss.co/genuinetools/pkg.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/pkg')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/pkg', 'git.j3ss.co/genuinetools/pkg', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/pkg.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/pkg.git')
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
