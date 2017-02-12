freeStyleJob('mirror_udict') {
    displayName('mirror-udict')
    description('Mirror github.com/jessfraz/udict to g.j3ss.co/udict.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/udict')
        sidebarLinks {
            link('https://git.j3ss.co/udict', 'git.j3ss.co/udict', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git clone --mirror git@github.com:jessfraz/udict.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/udict.git')
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
