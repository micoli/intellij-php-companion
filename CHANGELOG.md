<!-- Keep a Changelog guide -> https://keepachangelog.com -->


## [Unreleased]

## [0.1.0] - 2025-06-23

## [0.5.0] - 2025-07-10
- chore: Make findUsagesWithProgress more responsive
- feat: Add CliDumper to JSON on custom popups
- feat: Add CliDumper to PHP on custom popups
- fix: Prevent opening the same search twice or more
- fix: Plugin is searching for a matching route forever

## [0.6.0] - 2025-07-15
- feat(exportSource): Add a statusBarWidget to toggle usage of contextualNamespaces and ignore file
- feat(exportSource): Add an action to ExportSource to Markdown scratchfile
- feat(exportSource): Allow to specify contextualNamespace recursively added to context
- feat(exportSource): Display number of tokens after export
- feat(exportSource): Allow to customize template in configuration
- feat(exportSource): Add an actions to export source through clipboard or popup

- fix(exportSource): Fix ignore file usage when exportingSourceToMarkdown

- doc(README): Add associates,clidumper and sourceToMarkdown

- chore: Display more readable configuration errors
- chore: Use list in FileListProcessor instead of array
- chore: Fix deprecation on project startup
- chore: Add a service to process file list and ignore rules
- chore: Move Popup classes to UI package

- refactor(tests): Cleanup tests packages and resources
- refactor: Use proper package names to uncapitalize

- style: Migrate to palantir formatter
- style: Move actions to dedicated packages
- style: Fix coding standard according to qodana

## [0.6.1] - 2025-07-17
- fix(messengerGotoDeclaration): Fix bad navigation when clicking on messenger bus
- fix(exportSource): Fix thymeleaf not present due to CVE

- chore(exportSource): Convert ExportSourceToMarkdownService to project service
- chore(peerNavigation): Convert PeerNavigationService to project service
- chore(messenger): Convert MessengerService to project service
- chore(attributeNavigation): Convert AttributeNavigationService to project service

- chore(exportSource): Remove useless template line
