freeStyleJob('mirror_cliaoke') {
    displayName('mirror-cliaoke')
    description('Mirror github.com/jessfraz/cliaoke to g.j3ss.co/cliaoke.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/cliaoke')
        sidebarLinks {
            link('https://git.j3ss.co/cliaoke', 'git.j3ss.co/cliaoke', 'notepad.png')
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
        shell('git clone --mirror git@github.com:jessfraz/cliaoke.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/cliaoke.git')
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
