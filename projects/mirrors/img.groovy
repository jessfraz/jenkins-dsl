freeStyleJob('mirror_img') {
    displayName('mirror-img')
    description('Mirror github.com/jessfraz/img to g.j3ss.co/img.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/img')
        sidebarLinks {
            link('https://git.j3ss.co/img', 'git.j3ss.co/img', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/img.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/img.git')
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
