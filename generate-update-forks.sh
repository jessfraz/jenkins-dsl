#!/bin/bash

# This script generates DSLs to update each of my public forks.

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

DEFAULT_PER_PAGE=20
LAST_PAGE=1

ignore_repos=( mac-dev-setup )

# get the last page from the headers
get_last_page(){
	local header=${1%%" rel=\"last\""*}
	header=${header#*"rel=\"next\""}
	header=${header%%">;"*}
	LAST_PAGE=$(echo "${header#*'&page='}" | bc 2>/dev/null)
}

generate_dsl(){
	local forked_repo=$1
	local name=$(basename $forked_repo)
	local upstream_repo=$2
	local primary_branch=$3

	if [[ "${ignore_repos[@]}" =~ ${name} ]]; then
		return
	fi

	if [[ "${upstream_repo}" == "moby/docker" ]]; then
		upstream_repo="moby/moby"
	elif [[ "${upstream_repo}" == "kata-containers/kata-runtime" ]]; then
		upstream_repo="kata-containers/runtime"
	fi

	rname=${name//-/_}
	file="${DIR}/projects/forks/${rname//./_}.groovy"

	echo "${file} | ${forked_repo} | ${upstream_repo}"

	cat <<-EOF > $file
freeStyleJob('update_fork_${rname//./_}') {
    displayName('update-fork-${name}')
    description('Rebase the primary branch (${primary_branch}) in ${forked_repo} fork.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/${forked_repo}')
        sidebarLinks {
            link('https://github.com/${upstream_repo}', 'UPSTREAM: ${upstream_repo}', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:${forked_repo}.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/${primary_branch}:refs/remotes/origin/${primary_branch}')
            }
            remote {
                url('https://github.com/${upstream_repo}.git')
                name('upstream')
                refspec('+refs/heads/${primary_branch}:refs/remotes/upstream/${primary_branch}')
            }
            branches('${primary_branch}', 'upstream/${primary_branch}')
            extensions {
                disableRemotePoll()
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git rebase upstream/${primary_branch}')
    }

    publishers {
        git {
            branch('origin', '${primary_branch}')
            pushOnlyIfSuccess()
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

        wsCleanup()
    }
}
EOF

}

get_repos(){
	local page=$1

	# send the request
	local response
	response=$(curl -i -sSL -H "${AUTH_HEADER}" -H "${API_HEADER}" "${URI}/users/${GITHUB_USER}/repos?per_page=${DEFAULT_PER_PAGE}&page=${page}")

	# seperate the headers and body into 2 variables
	local head=true
	local header
	local body
	while read -r line; do
		if $head; then
			if [[ $line = $'\r' ]]; then
				head=false
			else
				header="$header"$'\n'"$line"
			fi
		else
			body="$body"$'\n'"$line"
		fi
	done < <(echo "${response}")

	get_last_page "${header}"

	local repos
	repos=$(echo "$body" | jq --raw-output '.[] | {fullname:.full_name,repo:.name,fork:.fork} | @base64')

	for r in $repos; do
		raw="$(echo "$r" | base64 -d)"
		local fullname
		fullname=$(echo "$raw" | jq --raw-output '.fullname')
		local repo
		repo=$(echo "$raw" | jq --raw-output '.repo')
		local fork
		fork=$(echo "$raw" | jq --raw-output '.fork')

		if [[ "$fork" == "true" ]]; then
			local response
			response=$(curl -sSL -H "${AUTH_HEADER}" -H "${API_HEADER}" "${URI}/repos/${fullname}")
			local upstream_user
			upstream_user=$(echo "$response" | jq --raw-output '.parent.owner.login')
			local primary_branch
			primary_branch=$(echo "$response" | jq --raw-output '.default_branch')
			generate_dsl "${fullname}" "${upstream_user}/${repo}" "${primary_branch}"
		fi
	done
}

main(){
	mkdir -p $DIR/projects/forks
	echo "FILE | FORK | UPSTREAM"

	local page=1

	get_repos "$page"

	if [ ! -z "$LAST_PAGE" ] && [ "$LAST_PAGE" -ge "$page" ]; then
		for page in  $(seq $((page + 1)) 1 "${LAST_PAGE}"); do
			# echo "[debug]: on repo page ${page} of ${LAST_PAGE}"
			get_repos "${page}"
		done
	fi
}

main
