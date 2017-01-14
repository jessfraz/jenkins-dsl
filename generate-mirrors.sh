#!/bin/bash


# This script generates DSLs for mirroring repositories. It used to do a
# real `git {clone,push} --mirror`, but since you need creds for it and the Git
# Plugin no longer saves the credentials in the workspace we can't call out to
# a shell to run --mirror. Also the git plugin doesn't have mirror options :(

set -e
set -o pipefail

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}"  )" && pwd  )"

if [[ -z "$GITHUB_USER" ]]; then
	echo "Set the GITHUB_USER env variable."
	exit 1
fi

if [[ -z "$GITHUB_TOKEN" ]]; then
    echo "Set the GITHUB_TOKEN env variable."
    exit 1
fi

URI=https://api.github.com
API_VERSION=v3
API_HEADER="Accept: application/vnd.github.${API_VERSION}+json"
AUTH_HEADER="Authorization: token ${GITHUB_TOKEN}"

DEFAULT_PER_PAGE=100

generate_dsl(){
	local orig=$1
	local user=${orig%/*}
	local name=${orig#*/}
	local primary_branch=$2

	rname=${name//-/_}
	file="${DIR}/projects/mirrors/${rname//./_}.groovy"

	if [[ "$GITHUB_USER" == "$user" ]]; then
		dest="$repo"
	else
		dest="$orig"
	fi

	echo "${file} | ${dest}"

	cat <<-EOF > $file
freeStyleJob('mirror_${rname//./_}') {
    displayName('mirror-${name}')
    description('Mirror github.com/${orig} to g.j3ss.co/${dest}.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/${orig}')
        sidebarLinks {
            link('https://git.j3ss.co/${dest}', 'git.j3ss.co/${dest}', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:${orig}.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/${primary_branch}:refs/remotes/origin/${primary_branch}')
            }
            remote {
                url('ssh://git@g.j3ss.co:2200/~/${dest}.git')
                name('mirror')
                credentials('ssh-github-key')
                refspec('+refs/heads/${primary_branch}:refs/remotes/upstream/${primary_branch}')
            }
            branches('${primary_branch}')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H/5 * * *')
    }

    wrappers { colorizeOutput() }

    publishers {
        postBuildScripts {
            git {
                branch('mirror', '${primary_branch}')
            }
        }

        extendedEmail {
            recipientList('\$DEFAULT_RECIPIENTS')
            contentType('text/plain')
            triggers {
                stillFailing {
                    attachBuildLog(true)
                }
            }
        }
    }
}
EOF

}


main(){
	# send the request
	local response
	response=$(curl -sSL -H "${AUTH_HEADER}" -H "${API_HEADER}" "${URI}/users/${GITHUB_USER}/repos?per_page=${DEFAULT_PER_PAGE}&type=public")
	local repos
	repos=$(echo "$response" | jq --raw-output '.[] | {fullname:.full_name,repo:.name,fork:.fork,description:.description} | @base64')

	mkdir -p $DIR/projects/mirrors
	echo "FILE | REPO"

	for r in $repos; do
		raw="$(echo "$r" | base64 -d)"
		local fullname
		fullname=$(echo "$raw" | jq --raw-output '.fullname')
		local repo
		repo=$(echo "$raw" | jq --raw-output '.repo')
		local description
		description=$(echo "$raw" | jq --raw-output '.description')
		local fork
		fork=$(echo "$raw" | jq --raw-output '.fork')

		response=$(curl -sSL -H "${AUTH_HEADER}" -H "${API_HEADER}" "${URI}/repos/${fullname}")
		local user
		user=$(echo "$response" | jq --raw-output '.parent.owner.login')
		local primary_branch
		primary_branch=$(echo "$response" | jq --raw-output '.default_branch')

		if [[ "$fork" == "true" ]]; then
			if [[ -z "$INCLUDE_FORKS" ]]; then
				continue
			else
				generate_dsl "${user}/${repo}"
			fi
		else
			generate_dsl "${fullname}" "${primary_branch}"
		fi
	done
}

main
