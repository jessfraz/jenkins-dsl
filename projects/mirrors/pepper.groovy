freeStyleJob('mirror_pepper') {
    displayName('mirror-pepper')
    description('Mirror github.com/jessfraz/pepper to g.j3ss.co/pepper.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/pepper')
        sidebarLinks {
            link('https://git.j3ss.co/pepper', 'git.j3ss.co/pepper', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/pepper.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/pepper.git')
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
