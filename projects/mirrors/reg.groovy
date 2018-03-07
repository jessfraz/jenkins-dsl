freeStyleJob('mirror_reg') {
    displayName('mirror-reg')
    description('Mirror github.com/jessfraz/reg to g.j3ss.co/reg.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/reg')
        sidebarLinks {
            link('https://git.j3ss.co/reg', 'git.j3ss.co/reg', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/reg.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/reg.git')
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
