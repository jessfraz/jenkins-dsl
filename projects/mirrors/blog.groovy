freeStyleJob('mirror_blog') {
    displayName('mirror-blog')
    description('Mirror github.com/jessfraz/blog to g.j3ss.co/blog.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/blog')
        sidebarLinks {
            link('https://git.j3ss.co/blog', 'git.j3ss.co/blog', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/blog.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/blog.git')
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
