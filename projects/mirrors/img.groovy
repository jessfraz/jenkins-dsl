freeStyleJob('mirror_img') {
    displayName('mirror-img')
    description('Mirror github.com/genuinetools/img to g.j3ss.co/genuinetools/img.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/img')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/img', 'git.j3ss.co/genuinetools/img', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/img.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/img.git')
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
