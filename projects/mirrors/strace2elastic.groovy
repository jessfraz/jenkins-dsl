freeStyleJob('mirror_strace2elastic') {
    displayName('mirror-strace2elastic')
    description('Mirror github.com/jessfraz/strace2elastic to g.j3ss.co/strace2elastic.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/strace2elastic')
        sidebarLinks {
            link('https://git.j3ss.co/strace2elastic', 'git.j3ss.co/strace2elastic', 'notepad.png')
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
        shell('git clone --mirror git@github.com:jessfraz/strace2elastic.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/strace2elastic.git')
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
