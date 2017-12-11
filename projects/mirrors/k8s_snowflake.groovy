freeStyleJob('mirror_k8s_snowflake') {
    displayName('mirror-k8s-snowflake')
    description('Mirror github.com/jessfraz/k8s-snowflake to g.j3ss.co/k8s-snowflake.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/k8s-snowflake')
        sidebarLinks {
            link('https://git.j3ss.co/k8s-snowflake', 'git.j3ss.co/k8s-snowflake', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/k8s-snowflake.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/k8s-snowflake.git')
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
