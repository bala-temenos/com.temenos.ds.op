Structure
=========

refex/ has "real" but just "example" modeling plug-ins.

base/ has build scripts etc. and raw low level infrastructure code shared between refex and in-house products. 


How to build
============

0. git clone --recursive
   or, if already cloned: git submodule update --init --recursive; git submodule status
1. Initially, just once: rm -rf mirror/; mvn -f mirror-pom.xml prepare-package
2. Then just mvn -o clean package! ;-)
3. rm -rf ../com.temenos.ds.op.sdk.ide/ ; cp -R base/releng/com.temenos.ds.op.base.sdk.repository/target/products/com.temenos.ds.op.sdk.ide.product/linux/gtk/x86_64/ ../com.temenos.ds.op.sdk.ide/
   $ env UBUNTU_MENUPROXY=0 SWT_GTK3=0 ../com.temenos.ds.op.sdk.ide/eclipse -consoleLog
   OR less base/releng/com.temenos.ds.op.base.sdk.repository/target/products/com.temenos.ds.op.sdk.ide.product/linux/gtk/x86_64/configuration/*.log

4. git pull; git submodule update (--remote)

The idea is to develop using the DS.open SDK Package which has everything needed (at the right version),
and not your own Eclipse download.  That product is built by this project, look around and learn how.

To speed up builds to the max, we normally operate in Maven offline mode;
set Window > Preferences > Maven: Offline, and dito in mvn Launch Configs 
(unless you've changed dependencies and need to reload something). -- We
typically also use the Maven Launcher in Eclipse (M2E), and not the CLI;
see the mvn.launch & mvn-o.launch in the various projects. 
We like option Resolve Workspace Artifacts.

Build the offline aggregate mirror of all needed p2 repos by opening the *.b3aggr, and right-click Build Aggregation.

How to make a change in a sobmodule
===================================

~/dev/ngMUI/com.temenos.ds.op$ git submodule status 
3d9bb0e2c60924b75436e6909d3a7fac76a46fc3 SUBS/eson (xtext-v2.4.1_beforeDowngrading-149-g3d9bb0e)

~/dev/ngMUI/com.temenos.ds.op$ cd SUBS/eson
~/dev/ngMUI/com.temenos.ds.op/SUBS/eson$ git branch
* (detached from 3d9bb0e)
  master
~/dev/ngMUI/com.temenos.ds.op/SUBS/eson$ git branch -a --contains 3d9bb0e
* (detached from 3d9bb0e)
  remotes/origin/eclipse.org
~/dev/ngMUI/com.temenos.ds.op/SUBS/eson$ git checkout eclipse.org 
Branch eclipse.org set up to track remote branch eclipse.org from origin.
Switched to a new branch 'eclipse.org'

git commit -am "... some change made in submodule... "
git push
cd ..
git diff SUBS/eson (WITHOUT TRAILING SLASH!)
git add SUBS/eson
git commit -am "move submodule to latest tip of eclipse.org branch"
git push

DO NOT use a forward slash after the submodule name in git add


How to add a new sobmodule
--------------------------

cd SUBS
git submodule add (-b master) [URL to Git repo]
git submodule init 

Recent Git (>=1.8.2) can track branch instead of commit with -b... (but commit has advantage better as more stable)
To fetch the latest from the remote master for this submodule you can then do:

git submodule update --remote

Note that this creates entries in /.gitmodules... this has to be adapted e.g. if you move SUBS directory (incl. branch info if -b).

To change the Git URL it doesn't seem to be sufficient to edit .gitmodules; I've had to git submodule deinit -f eson/; rm -rf eson/; git submodule add http://git.eclipse.org/gitroot/emf/org.eclipse.emf.eson.git; git submodule init; nano ../.gitmodules to clean-up.

@see http://www.vogella.com/tutorials/Git/article.html#submodules


How to add features
===================

1. add new remote p2 repo to ds.open/base/releng/com.temenos.ds.op.base.parent/pom.xml
2. if you happen to have ;) an in-house Nexus proxy, don't forget to have the same added there
3. add req. features to ds.open/base/features/com.temenos.ds.op.base.feature/feature.xml (if it's needed for end-users)
   and respective source feature (or both, if it's for Dev only) to ds.open/base/features/com.temenos.ds.op.base.sdk.feature/feature.xml
   Note that it's possible to add both <import feature=".."/> and <import plugin="..."/>
   (Note that, contrary to e.g. *.target file, you don't need precise version here.)
4. rebuild quickly locally & "eat your own dog food"


Various Tips & Tricks
=====================

In each *.feature, remove the url= and use version="0.0.0" instead of version="1.0.0.qualifier" inside <feature ...>.
Remember that in *.feature sometimes (but not always) you have to append .feature in <includes id="...feature"> - does NOT match JAR filename.

In each *.product, in Dependencies, remove the Version of each Feature.

In category.xml <feature> remove fixed version numbers in url and version and use .qualifier.

Why we don't use *.target platform definition files, and instead just specify the required <repository><layout>p2</layout> p2 repos in pom.xml.  One of the advantages of this approach is that it allows Maven settings.xml <mirror><id> to work.  Another advantate is that you stop wasting time configuring *.target.. ;-) (even though https://github.com/mbarbero/fr.obeo.releng.targetplatform can offset that pain).  The disadvantage of that is that there is no *.target to load into a completely fresh PDE. We accept this limitation, as we expect developers will use only our SDK product built with this ("eat your own dogfood").

