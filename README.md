# jenkins-dsl
Jenkins DSLs for my private Jenkins instance, keeps forks up to date, mirrors
repositories to private git, builds all Dockerfiles and more.

The [plugins.txt](plugins.txt) file documents all the extra plugins needed to
get all the features.

## Using the Makefile

```console
$ make help
all                            Run all the generation scripts.
deploy-key                     Create a deploy key for a given repo (ex. REPO=jessfraz/jenkins-dsl).
projects/forks                 Generate DSLs to update git forks.
projects/mirrors               Generate DSLs for git mirrors.
projects/repo-dockerfiles      Generate DSLs for repos with Dockerfiles.
webhook-jenkins-create         Create the jenkins webhook for a given repo (ex. REPO=jessfraz/jenkins-dsl).
webhook-jenkins-get            Get the jenkins webhook for a given repo (ex. REPO=jessfraz/jenkins-dsl).
webhook-travis-get             Get a travis webhook for a given repo (ex. REPO=jessfraz/jenkins-dsl).
```
