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

DEFAULT_PER_PAGE=20
LAST_PAGE=1

# get the last page from the headers
get_last_page(){
	local header=${1%%" rel=\"last\""*}
	header=${header#*"rel=\"next\""}
	header=${header%%">;"*}
	LAST_PAGE=$(echo "${header#*'&page='}" | bc 2>/dev/null)
}

generate_dsl(){
	local orig=$1
	local user=${orig%/*}
	local name=${orig#*/}

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
        numToKeep(100)
        daysToKeep(15)
    }
    triggers {
        cron('H H * * *')
    }
    wrappers { colorizeOutput() }
    steps {
        shell('git clone --mirror https://github.com/${orig}.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/${dest}.git')
    }
    publishers {
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
	local org=$2

	# send the request
	local response
	if [[ -z "$org" ]]; then
		response=$(curl -i -sSL -H "${AUTH_HEADER}" -H "${API_HEADER}" "${URI}/users/${GITHUB_USER}/repos?per_page=${DEFAULT_PER_PAGE}&type=public&page=${page}")
	else
		response=$(curl -i -sSL -H "${AUTH_HEADER}" -H "${API_HEADER}" "${URI}/orgs/${2}/repos?per_page=${DEFAULT_PER_PAGE}&type=public&page=${page}")
	fi


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
	repos=$(echo "$body" | jq --raw-output '.[] | {fullname:.full_name,repo:.name,fork:.fork,description:.description} | @base64')

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

		if [[ "$fullname" == "${GITHUB_USER}/linux" ]]; then
			continue
		fi

		if [[ "$fork" == "true" ]]; then
			if [[ -z "$INCLUDE_FORKS" ]]; then
				continue
			else
				generate_dsl "${user}/${repo}"
			fi
		else
			generate_dsl "${fullname}"
		fi
	done
}

main(){
	mkdir -p $DIR/projects/mirrors
	echo "FILE | REPO"

	# Get the personal repos.
	local page=1

	get_repos "$page"

	if [ ! -z "$LAST_PAGE" ] && [ "$LAST_PAGE" -ge "$page" ]; then
		for page in  $(seq $((page + 1)) 1 "${LAST_PAGE}"); do
			# echo "[debug]: on repo page ${page} of ${LAST_PAGE}"
			get_repos "${page}"
		done
	fi

	# Get the genuinetools organization repos.
	LAST_PAGE=1
	local page=1

	get_repos "$page" "genuinetools"

	if [ ! -z "$LAST_PAGE" ] && [ "$LAST_PAGE" -ge "$page" ]; then
		for page in  $(seq $((page + 1)) 1 "${LAST_PAGE}"); do
			# echo "[debug]: on repo page ${page} of ${LAST_PAGE}"
			get_repos "${page}" "genuinetools"
		done
	fi
}

main
