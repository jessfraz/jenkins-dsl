all: projects/mirrors projects/repo-dockerfiles projects/forks

.PHONY: projects/mirrors
projects/mirrors: generate-mirrors.sh
	@echo "+ $@"
	./$< | column -t

projects/repo-dockerfiles: generate-repo-dockerfiles.sh
	@echo "+ $@"
	./$< | column -t

.PHONY: projects/forks
projects/forks: generate-update-forks.sh
	@echo "+ $@"
	./$< | column -t
