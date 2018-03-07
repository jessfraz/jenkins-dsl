freeStyleJob('mirror_directory_theme') {
    displayName('mirror-directory-theme')
    description('Mirror github.com/jessfraz/directory-theme to g.j3ss.co/directory-theme.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/directory-theme')
        sidebarLinks {
            link('https://git.j3ss.co/directory-theme', 'git.j3ss.co/directory-theme', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/directory-theme.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/directory-theme.git')
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
