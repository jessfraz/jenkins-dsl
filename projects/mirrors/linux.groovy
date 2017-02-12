freeStyleJob('mirror_linux') {
    displayName('mirror-linux')
    description('Mirror github.com/jessfraz/linux to g.j3ss.co/linux.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/linux')
        sidebarLinks {
            link('https://git.j3ss.co/linux', 'git.j3ss.co/linux', 'notepad.png')
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
        shell('git clone --mirror git@github.com:jessfraz/linux.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/linux.git')
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
