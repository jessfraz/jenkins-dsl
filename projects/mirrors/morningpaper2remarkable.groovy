freeStyleJob('mirror_morningpaper2remarkable') {
    displayName('mirror-morningpaper2remarkable')
    description('Mirror github.com/jessfraz/morningpaper2remarkable to g.j3ss.co/morningpaper2remarkable.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/morningpaper2remarkable')
        sidebarLinks {
            link('https://git.j3ss.co/morningpaper2remarkable', 'git.j3ss.co/morningpaper2remarkable', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/morningpaper2remarkable.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/morningpaper2remarkable.git')
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
