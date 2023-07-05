# Contributing to the Project

Thank you for your interest in contributing to
the Eclipse Dataspace Testkit

## Table of Contents

* [Code Of Conduct](#code-of-conduct)
* [Eclipse Contributor Agreement](#eclipse-contributor-agreement)
* [How to Contribute](#how-to-contribute)
  * [Discuss](#discuss)
  * [Create an Issue](#create-an-issue)
  * [Submit a Pull Request](#submit-a-pull-request)
  * [Report on Flaky Tests](#report-on-flaky-tests)
* [Project and Milestone Planning](#project-and-milestone-planning)
  * [Milestones](#milestones)
  * [Projects](#projects)
  * [Releases](#releases)
* [Contact Us](#contact-us)

## Code Of Conduct

See the [Eclipse Code Of Conduct](https://www.eclipse.org/org/documents/Community_Code_of_Conduct.php).

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project, you need to create and electronically sign
a [Eclipse Contributor Agreement (ECA)](http://www.eclipse.org/legal/ecafaq.php):

1. Log in to the [Eclipse foundation website](https://accounts.eclipse.org/user/login/). You will 
   need to create an account within the Eclipse Foundation if you have not already done so.
2. Click on "Eclipse ECA", and complete the form.

Be sure to use the same email address in your Eclipse Account that you intend to use when you commit 
to GitHub.

## How to Contribute

### Create an Issue

If you have identified a bug or want to formulate a working item that you want to concentrate on, 
feel free to create a new issue at our project's corresponding
[GitHub Issues page](https://github.com/eclipse-dataspacetck/cvf/issues/new).

Before doing so, please consider searching for potentially suitable
[existing issues](https://github.com/eclipse-dataspacetck/cvf/issues?q=is%3Aissue+is%3Aopen).

We also use [GitHub's default label set](https://docs.github.com/en/issues/using-labels-and-milestones-to-track-work/managing-labels)
extended by custom ones to classify issues and improve discoverability.

If an issue appears to cover changes that will have a (huge) impact on the code base and needs to 
first be discussed, or if you just have a question regarding the usage of the software, please 
create an issue with the `Question` label.

Please note that if an issue covers a topic or the response to a question that may be interesting 
for other developers or contributors, or for further discussions, it should be converted to a 
discussion and not be closed.

### Adhere to Coding Style Guide

We aim for a coherent and consistent code base, thus the coding style detailed in the 
[styleguide](styleguide.md) should be followed.

### Submit a Pull Request

In addition to the contribution guideline made available in the 
[Eclipse project handbook](https://www.eclipse.org/projects/handbook/#contributing),
we would appreciate if your pull request applies to the following points:

* Conform to [Pull-Request Etiquette](pr_etiquette.md)

* Always apply the following copyright header to specific files in your work replacing the fields 
  enclosed by curly brackets "{}" with your own identifying information. (Don't include the curly 
  brackets!) Enclose the text in the appropriate comment syntax for the file format.

    ```text
    Copyright (c) {year} {owner}[ and others]

    This program and the accompanying materials are made available under the
    terms of the Apache License, Version 2.0 which is available at
    https://www.apache.org/licenses/LICENSE-2.0

    SPDX-License-Identifier: Apache-2.0

    Contributors:
      {name} - {description}
    ```

* The git commit messages should comply to the following format:
    ```
    <prefix>(<scope>): <description>
    ```

  Use the [imperative mood](https://github.com/git/git/blob/master/Documentation/SubmittingPatches)
  as in "Fix bug" or "Add feature" rather than "Fixed bug" or "Added feature" and
  [mention the GitHub issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue)
  e.g. `chore(transfer process): improve logging`.

  All committers, and all commits, are bound to
  the [Developer Certificate of Origin.](https://www.eclipse.org/legal/DCO.php)
  As such, all parties involved in a contribution must have valid ECAs. Additionally, commits can 
  include a ["Signed-off-by" entry](https://wiki.eclipse.org/Development_Resources/Contributing_via_Git).
  
* Add meaningful tests to verify your submission acts as expected.

* Where code is not self-explanatory, add documentation providing extra clarification.

* Add documentation files to new modules. See [here](#add-documentation) for more details.

* If a new module has been added or a significant part of the code has been changed and you should 
  or want to be seen as the contact person for any further changes, please add appropriate 
  information to the [CODEOWNERS](https://github.com/eclipse-dataspacetck/cvf/blob/main/CODEOWNERS) 
  file. You can find instructions on how to do this at <https://help.github.com/articles/about-codeowners/>.
  Please note that this file does not represent all contributions to the code. What persons and organizations
  actually contributed to each file can be seen on GitHub and is documented in the license headers.

* PR descriptions should use the current [PR template](.github/PULL_REQUEST_TEMPLATE.md)

* Submit a draft pull request at early-stage and add people previously working on the same code as 
  reviewer. Make sure automatic checks pass before marking it as "ready for review":

    * _Intellectual Property Validation_ verifying the [Eclipse CLA](#eclipse-contributor-agreement) 
      has been signed as well as commits have been signed-off and
    * _Continuous Integration_ performing various test conventions.

### Stale issues and PRs

In order to keep our backlog clean we are using a bot that helps us label and eventually close old issues and PRs. The
following table shows the particular timings.

|                        | `stale` after | closed after days `stale` |
|------------------------|---------------|---------------------------|
| Issue without assignee | 14            | 7                         |
| Issue with assignee    | 28            | 7                         |
| PR                     | 7             | 7                         |

Note that updating an issue, e.g. by commenting, will remove the `stale` label again and reset the counters. However,
we ask the community **not to abuse** this feature (e.g. commenting "what's the status?" every X days would certainly 
be qualified as abuse). If an issue receives no attention, there usually
are reasons for it. It is therefore advisable to clarify in advance whether any particular feature fits into EDC's
planning schedule and roadmap. For that, we recommend opening a discussion. Discussions serve us as a system of record, that 
means we monitor them more closely, and do not close them automatically.


#### Issues

Every issue that should be addressed during a milestone phase is assigned to it by using the 
`Milestone` feature for linking both items. This way, the issues can easily be filtered by 
milestones.

#### Pull Requests

Pull requests are not assigned to milestones as their linking to issues is sufficient to track 
the relations and progresses.
