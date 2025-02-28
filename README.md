Smack
=====

[![Build Status](https://github.com/igniterealtime/Smack/workflows/CI/badge.svg)](https://github.com/igniterealtime/Smack/actions?query=workflow%3A%22CI%22)  [![Coverage Status](https://coveralls.io/repos/igniterealtime/Smack/badge.svg)](https://coveralls.io/r/igniterealtime/Smack)  [![Project Stats](https://www.openhub.net/p/smack/widgets/project_thin_badge.gif)](https://www.openhub.net/p/smack)  [![Link to XMPP chat smack@conference.igniterealtime.org](https://search.jabber.network/api/1.0/badge?address=smack@conference.igniterealtime.org)](https://inverse.chat/#converse/room?jid=smack@conference.igniterealtime.org)

About
-----

[Smack] is an open-source, highly modular, easy to use, XMPP client library written in Java for Java SE compatible JVMs and Android.

Being a pure Java library, it can be embedded into your applications to create anything from a full XMPP instant messaging client to simple XMPP integrations such as sending notification messages and presence-enabling devices.
Smack and XMPP allow you to easily exchange data in various ways e.g., fire-and-forget, publish-subscribe, between human and non-human endpoints (M2M, IoT, …).

More information is provided by the [Overview](documentation/overview.md).


Getting started
---------------

Start with having a look at the **[Documentation]** and the **[Javadoc]**.

Instructions on how to use Smack in your Java or Android project are provided in the [Smack Readme and Upgrade Guide](https://igniterealtime.org/projects/smack/readme).

License
-------

Most of Smack is governed by the Apache License 2.0 (SPDX License Identifier: Apache 2.0). This license requires that the contents of a NOTICE text file are shown "…within a display generated by the Derivative Works, if and wherever such third-party notices normally appear.".

Smack comes which such a NOTICE file. Moreover, since `smack-core` is licensed under the Apache License 2.0, the conditions apply to every project using Smack. The content of Smack's NOTICE file can conveniently be retrieved using `Smack.getNoticeStream()`.

Some subprojects of Smack are governed by other licenses. Please refer to the individual subprojects.

Professional Services
---------------------

Smack is a collaborative effort of many people.

Some are paid, e.g., by their employer or a third party, for their contributions.
But many contribute in their spare time for free.
While we try to provide the best possible XMPP library for Android and Java SE-compatible execution environments by following state-of-the-art software engineering practices, the API may not always perfectly fit your requirements.
Hence welcome contributions and encourage discussion about how Smack can be further improved.
We also provide **paid services** ranging from **XMPP/Smack related consulting** to **designing and developing features** to accommodate your needs.
Please contact [Florian Schmaus](mailto:flo@geekplace.eu) for further information.

Bug Reporting
-------------

Only a few users have access for filling bugs in the tracker. New
users should:

1. Read ["How to ask for help or report an issue"](https://github.com/igniterealtime/Smack/wiki/How-to-ask-for-help,-report-an-issue-and-possible-solve-the-problem-yourself)
2. [Create a discourse account](https://discourse.igniterealtime.org/signup) (you can also sign up with your Google account).
3. Login to the forum account
4. Press "New Topic" in your toolbar and choose the 'Smack Support' sub-category.

Please search for your issues in the bug tracker before reporting.

Contact
-------

The developers hang around in [smack@conference.igniterealtime.org](xmpp:smack@conference.igniterealtime.org?join). You may use [this link](https://inverse.chat/badge.svg?room=smack@conference.igniterealtime.org) to join the room via [inverse.chat](https://inverse.chat).
Remember that it may take some time (~hours) to get a response.
 
You can also reach us via the [Smack Support Forum] if you have questions or need support, or the [Smack Developers Forum] if you want to discuss Smack development.

Contributing
------------

If you want to start developing for Smack and eventually contribute code back, then please have a look at the [Guidelines for Smack Developers and Contributors](https://github.com/igniterealtime/Smack/wiki/Guidelines-for-Smack-Developers-and-Contributors).
The guidelines also contain development quickstart instructions.

Resources
---------

- Current Readme: https://igniterealtime.org/projects/smack/readme
- Bug Tracker: https://issues.igniterealtime.org/browse/SMACK
- JaCoCo Coverage Reports: https://www.igniterealtime.org/builds/smack/dailybuilds/jacoco/html/
- Nightly Javadoc: http://www.igniterealtime.org/builds/smack/dailybuilds/javadoc/
- Nightly Documentation: http://www.igniterealtime.org/builds/smack/dailybuilds/documentation/
- User Forum: https://discourse.igniterealtime.org/c/smack/smack-support
- Dev Forum: https://discourse.igniterealtime.org/c/smack/smack-dev
- Maven Releases: https://oss.sonatype.org/content/repositories/releases/org/igniterealtime/smack/
- Maven Snapshots: https://oss.sonatype.org/content/repositories/snapshots/org/igniterealtime/smack/
- Nightly Unique Maven Snapshots: https://www.igniterealtime.org/archiva/repository/maven/

Ignite Realtime
===============

[Ignite Realtime] is an Open Source community composed of end-users and developers around the world who are interested in applying innovative, open-standards-based RealTime Collaboration to their businesses and organizations. 
We're aimed at disrupting proprietary, non-open standards-based systems and invite you to participate in what's already one 
of the biggest and most active Open Source communities.

[Smack] - an [Ignite Realtime] community project.

[Smack]: https://www.igniterealtime.org/projects/smack/index.jsp
[Ignite Realtime]: https://www.igniterealtime.org
[XMPP (Jabber)]: https://xmpp.org/
[Smack Developers Forum]: https://discourse.igniterealtime.org/c/smack/smack-dev
[Smack Support Forum]: https://discourse.igniterealtime.org/c/smack/smack-support
[Documentation]: https://download.igniterealtime.org/smack/docs/latest/documentation/
[Javadoc]: https://download.igniterealtime.org/smack/docs/latest/javadoc/
