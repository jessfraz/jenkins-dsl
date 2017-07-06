freeStyleJob('mirror__vim') {
    displayName('mirror-.vim')
    description('Mirror github.com/jessfraz/.vim to g.j3ss.co/.vim.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/.vim')
        sidebarLinks {
            link('https://git.j3ss.co/.vim', 'git.j3ss.co/.vim', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/.vim.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/.vim.git')
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
