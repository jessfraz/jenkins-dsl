freeStyleJob('mirror_netns') {
    displayName('mirror-netns')
    description('Mirror github.com/jessfraz/netns to g.j3ss.co/netns.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/netns')
        sidebarLinks {
            link('https://git.j3ss.co/netns', 'git.j3ss.co/netns', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/netns.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/netns.git')
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
