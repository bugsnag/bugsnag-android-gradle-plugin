bump:
ifeq ($(VERSION),)
	@$(error VERSION is not defined. Run with `make VERSION=number bump`)
endif
	@echo Bumping the version number to $(VERSION)
	@sed -i '' "s/version = .*/version = $(VERSION)/" gradle.properties
	@sed -i '' "s/## TBD/## $(VERSION) ($(shell date '+%Y-%m-%d'))/" CHANGELOG.md


# Makes a release and pushes to github
release:
ifeq ($(VERSION),)
	@$(error VERSION is not defined. Run with `make VERSION=number release`)
endif
	@git add -p gradle.properties CHANGELOG.md
	@git commit -m "Release v$(VERSION)"
	@git tag v$(VERSION)
	@git push origin master v$(VERSION)
	@./gradlew clean uploadArchives bintrayUpload publishPlugins
