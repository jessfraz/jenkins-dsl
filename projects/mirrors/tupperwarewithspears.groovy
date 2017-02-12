freeStyleJob('mirror_tupperwarewithspears') {
    displayName('mirror-tupperwarewithspears')
    description('Mirror github.com/jessfraz/tupperwarewithspears to g.j3ss.co/tupperwarewithspears.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/tupperwarewithspears')
        sidebarLinks {
            link('https://git.j3ss.co/tupperwarewithspears', 'git.j3ss.co/tupperwarewithspears', 'notepad.png')
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
        shell('git clone --mirror git@github.com:jessfraz/tupperwarewithspears.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/tupperwarewithspears.git')
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
