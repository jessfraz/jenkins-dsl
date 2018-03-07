freeStyleJob('mirror_apparmor_docs') {
    displayName('mirror-apparmor-docs')
    description('Mirror github.com/jessfraz/apparmor-docs to g.j3ss.co/apparmor-docs.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/apparmor-docs')
        sidebarLinks {
            link('https://git.j3ss.co/apparmor-docs', 'git.j3ss.co/apparmor-docs', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/apparmor-docs.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/apparmor-docs.git')
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
