freeStyleJob('mirror_dotfiles') {
    displayName('mirror-dotfiles')
    description('Mirror github.com/jessfraz/dotfiles to g.j3ss.co/dotfiles.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/dotfiles')
        sidebarLinks {
            link('https://git.j3ss.co/dotfiles', 'git.j3ss.co/dotfiles', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/dotfiles.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/dotfiles.git')
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
