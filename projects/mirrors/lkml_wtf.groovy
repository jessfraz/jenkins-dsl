freeStyleJob('mirror_lkml_wtf') {
    displayName('mirror-lkml.wtf')
    description('Mirror github.com/jessfraz/lkml.wtf to g.j3ss.co/lkml.wtf.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/lkml.wtf')
        sidebarLinks {
            link('https://git.j3ss.co/lkml.wtf', 'git.j3ss.co/lkml.wtf', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/lkml.wtf.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/lkml.wtf.git')
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
