freeStyleJob('mirror_netns') {
    displayName('mirror-netns')
    description('Mirror github.com/genuinetools/netns to g.j3ss.co/genuinetools/netns.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/netns')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/netns', 'git.j3ss.co/genuinetools/netns', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/netns.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/netns.git')
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
