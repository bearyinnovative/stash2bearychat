# Stash2BearyChat

Plugin subscribes to Stash server events and send notifications to
bearychat channels.

Currently the following events are supported:

* PullRequestActivity - Event that is raised when an activity is created for a pull request.
* RepositoryPushEvent - Event that is raised when a user pushes one or more refs to a repository

## Build
use atlassian to build instead of maven, see https://developer.atlassian.com/docs/getting-started/set-up-the-atlassian-plugin-sdk-and-build-a-project

use

```bash
atlas-mvn package
```

## Installation

Grab the latest release, and upload it to your stash instance on
the manage addons page.

## Configuration

You need to create an incoming web hook in bearychat. That will give you the
hook url and the default channel name. Notifications will go to the
defaul bearychat channel, unless you override them in the configuration for
a given repository.

You can enter the webhook url in the global settings. Just go to
http://your.stash.host/plugins/servlet/bearychat-global-settings/admin and
edit the hook url.

Then for each repository, you can enable the notifications. Go to your
repository page, select Settings, BearyChat settings. There are two check boxes,
one for enabling notifications for pull requests, and one for push activities.

You can also enter a custom channel name here, if you want your notifications
to go to a different channel, and also a custom webhook url which overrides
the default hook url, if it is set.
