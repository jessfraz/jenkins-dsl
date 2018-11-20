freeStyleJob('mirror_branch_cleanup_action') {
    displayName('mirror-branch-cleanup-action')
    description('Mirror github.com/jessfraz/branch-cleanup-action to g.j3ss.co/branch-cleanup-action.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/branch-cleanup-action')
        sidebarLinks {
            link('https://git.j3ss.co/branch-cleanup-action', 'git.j3ss.co/branch-cleanup-action', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/branch-cleanup-action.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/branch-cleanup-action.git')
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
