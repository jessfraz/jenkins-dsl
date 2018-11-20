freeStyleJob('mirror_aws_fargate_action') {
    displayName('mirror-aws-fargate-action')
    description('Mirror github.com/jessfraz/aws-fargate-action to g.j3ss.co/aws-fargate-action.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/aws-fargate-action')
        sidebarLinks {
            link('https://git.j3ss.co/aws-fargate-action', 'git.j3ss.co/aws-fargate-action', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/aws-fargate-action.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/aws-fargate-action.git')
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
