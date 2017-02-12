freeStyleJob('mirror_notorious_svg') {
    displayName('mirror-notorious-svg')
    description('Mirror github.com/jessfraz/notorious-svg to g.j3ss.co/notorious-svg.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/notorious-svg')
        sidebarLinks {
            link('https://git.j3ss.co/notorious-svg', 'git.j3ss.co/notorious-svg', 'notepad.png')
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
        shell('git clone --mirror git@github.com:jessfraz/notorious-svg.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/notorious-svg.git')
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
