freeStyleJob('mirror_generator_xbars') {
    displayName('mirror-generator-xbars')
    description('Mirror github.com/jessfraz/generator-xbars to g.j3ss.co/generator-xbars.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/generator-xbars')
        sidebarLinks {
            link('https://git.j3ss.co/generator-xbars', 'git.j3ss.co/generator-xbars', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/generator-xbars.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/generator-xbars.git')
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
