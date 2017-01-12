#!/bin/bash

# This script creates, edits, deletes, or gets your jenkins webhooks for specific repos.

set -e
set -o pipefail

if [[ -z "$GITHUB_TOKEN" ]]; then
	echo "Set the GITHUB_TOKEN env variable."
	exit 1
fi

URI=https://api.github.com
API_VERSION=v3
API_HEADER="Accept: application/vnd.github.${API_VERSION}+json"
AUTH_HEADER="Authorization: token ${GITHUB_TOKEN}"
JENKINS_HOOK_URL="${JENKINS_HOOK_URL:-https://jenkins.j3ss.co/github-webhook/}"

create_hook(){
	repo=$1
	repo="${repo%%/hooks*}"

	echo "Creating jenkins webhook for $repo"

	curl -i -sSL -XPOST -H "${AUTH_HEADER}" -H "${API_HEADER}" --data-binary @- "${URI}/repos/${repo}/hooks" <<-EOF
	{
		"name": "jenkins",
		"active": true,
		"events": ["push"],
		"config": {
		"url": "${JENKINS_HOOK_URL}",
		"jenkins_hook_url": "${JENKINS_HOOK_URL}",
		"content_type": "form"
	}
}
EOF
}

edit_hook(){
	repo=$1

	echo "Editing jenkins webhook for $repo"

	curl -i -sSL -XPATCH -H "${AUTH_HEADER}" -H "${API_HEADER}" --data-binary @- "${URI}/repos/${repo}" <<-EOF
	{
		"name": "jenkins",
		"active": true,
		"events": ["push"],
		"config": {
		"url": "${JENKINS_HOOK_URL}",
		"jenkins_hook_url": "${JENKINS_HOOK_URL}",
		"content_type": "form"
	}
}
EOF
}

get_hook(){
	repo=$1

	echo "Getting jenkins webhook for $repo"

	curl -sSL -H "${AUTH_HEADER}" -H "${API_HEADER}" "${URI}/repos/${repo}" | jq .config.jenkins_hook_url
}

delete_hook(){
	repo=$1

	echo "Deleting jenkins webhook for $repo"

	curl -i -sSL -XDELETE -H "${AUTH_HEADER}" -H "${API_HEADER}" "${URI}/repos/${repo}"
}

main(){
	action=$1
	repo=$2

	if [[ "$action" == "" ]]; then
		echo "Pass an action as the first argument: ex. create, edit, get, delete" >&2
		exit 1
	fi

	if [[ "$repo" == "" ]]; then
		echo "Pass a repo as the second argument: ex. jessfraz/jenkins-dsl" >&2
		exit 1
	fi

	case $action in
		"create")
			create_hook "$2"
			;;
		"edit")
			edit_hook "$2"
			;;
		"get")
			get_hook "$2"
			;;
		"delete")
			delete_hook "$2"
			;;
		*)
			echo "Invalid action ${action}. Try: create, edit, get, delete" >&2
			exit 1
			;;
	esac
}

main $@
