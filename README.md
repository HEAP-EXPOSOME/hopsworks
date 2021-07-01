<a href=""><img src="https://content.logicalclocks.com/hubfs/37C9E408-B200-4166-A2C0-0C85E6FFAEB4_4_5005_c.jpeg" align="center"></a>

<p align="center">
    <a href="https://hopsworks.ai" alt="hopsworks.ai">
        <img src="https://img.shields.io/badge/hopsworks-ai-brightgreen" /></a>
    <a href="https://docs.hopsworks.ai" alt="docs.hopsworks.ai">
        <img src="https://img.shields.io/badge/hopsworks-docs-orange" /></a>
    <a href="https://community.hopsworks.ai" alt="community.hopsworks.ai">
        <img src="https://img.shields.io/badge/hopsworks-community-blueviolet" /></a>
    <a href="https://twitter.com/hopsworks" alt="Hopsworks Twitter">
        <img src="https://img.shields.io/badge/hopsworks-twitter-blue" /></a>
</p>

<p align="center">
  Give us a star if you appreciate what we do <img src="https://content.logicalclocks.com/hubfs/1f49a.png">
</p>

[ What is Hopsworks? ](#what)  
[ Quick Start ](#quick)  
[ Development and Operational ML on Hopsworks ](#mlops)  
[ Docs ](#docs)  
[ Who’s behind Hopsworks? ](#who)  
[ Open-Source](#open)  
[ Join the community ](#join)  
[ Contribute ](#contribute)  


<a name="what"></a>
## What is Hopsworks?

Hopsworks and its Feature Store are an open source data-intensive AI platform used for the development and operation of machine learning models at scale.

<img src="docs/architecture.svg">

<a name="quick"></a>
## Quick Start


```bash
bash <(curl -s https://repo.hops.works/installer/latest/hopsworks-installer.sh)
```
Recommended minimum [specification](https://hopsworks.readthedocs.io/en/stable/getting_started/installation_guide/platforms/hopsworks-installer.html#requirements). 

The [Hopsworks Installer](https://hopsworks.readthedocs.io/en/stable/getting_started/installation_guide/platforms/hopsworks-installer.html) takes roughly 1-2 hrs to complete, depending on your bandwidth. 


### Installer Quick Start with Azure CLI or GCP CLI

```python
bash <(curl -s https://repo.hops.works/installer/latest/hopsworks-cloud-installer.sh)
```
If you have the Azure or GCP CLI utilities installed (on a Linux machine), then the [Hopsworks Cloud Installer](https://hopsworks.readthedocs.io/en/stable/getting_started/installation_guide/platforms/hopsworks-cloud-installer.html) (Hopsworks-cloud-installer.sh) will both install Hopsworks and provision the VMs in one command. 

### Hopsworks IDE Plugin

To work with the Hopsworks IDE plugin for IntelliJ/PyCharm, you can install it directly from the plugins menu of the IDE or [clone it](https://github.com/logicalclocks/hopsworks-ide-plugins) and follow the README.

### Build with Maven
```sh
mvn install
```
Maven uses yeoman-maven-plugin to build both the front-end and the backend.
Maven first executes the Gruntfile in the yo directory, then builds the back-end in Java.
The yeoman-maven-plugin copies the dist folder produced by grunt from the yo directory to the target folder of the backend.

You can also build Hopsworks without the frontend (for Java EE development and testing):
```sh
mvn install -P-web
```

<a name="mlops"></a>
## Development and Operational ML on Hopsworks
You can develop and run Python, Spark, and Flink applications on Hopsworks - in Jupyter notebooks, as jobs, or even notebooks as jobs. You can build production pipelines with the bundled Airflow, and even run ML training pipelines with GPUs in notebooks on Airflow. You can train models on as many GPUs as are installed in a Hopsworks cluster and easily share them among users.

<a name="docs"></a>
## Docs
[Hopsworks documentation](https://hopsworks.readthedocs.io/en/latest/overview/overview.html#) includes a user-guide, Feature Store documentation, and an Administrator Guide. There is also dedicated [documentation for the Hopsworks Feature Store](https://docs.hopsworks.ai/feature-store-api/latest/).

Hopsworks REST API is documented with Swagger and hosted by SwaggerHub.
- **hopsworks-api** - [https://app.swaggerhub.com/apis-docs/logicalclocks/hopsworks-api](https://app.swaggerhub.com/apis-docs/logicalclocks/hopsworks-api)
- **hopsworks-ca** - [https://app.swaggerhub.com/apis-docs/logicalclocks/hopsworks-ca](https://app.swaggerhub.com/apis-docs/logicalclocks/hopsworks-ca)

To build and deploy swagger on your own Hopsworks instance you can follow the instructions found in 
[this](https://hopsworks.readthedocs.io/en/stable/user_guide/hopsworks/microservices.html?highlight=swagger#swagger-ui) guide.


<a name="who"></a>
## Who’s behind Hopsworks?
Hopsworks started as an open-source collaborative project at [KTH University](https://www.kth.se/en), [RISE](https://www.ri.se/en), and has more recently been taken on by [Logical Clocks](https://www.logicalclocks.com/). Several funding bodies have helped contribute to its development including: [European Commission (FP7, H2020)](https://ec.europa.eu/), [EIT](https://eit.europa.eu/), [SSF](https://strategiska.se/), [Vinnova](https://www.vinnova.se/) and [Celtic-Next](https://www.celticnext.eu/).


<a name="open"></a>
## Open-Source
Hopsworks is available under the AGPL-V3 license. In plain English this means that you are free to use Hopsworks and even build paid services on it, but if you modify the source code, you should also release your changes and any systems built around it as AGPL-V3.


<a name="join"></a>
## Join the community
-  Ask questions and give us feedback in the [Hopsworks Community](https://community.hopsworks.ai/)
- Follow us on [Twitter](https://twitter.com/hopsworks)
- Check out all our latest [product releases](https://github.com/logicalclocks/hopsworks/releases)


<a name="contribute"></a>
## Contribute
We are building the most complete and modular ML platform available in the market and we count on your support to continuously improve Hopsworks. Feel free to [give us suggestions](https://github.com/logicalclocks/feature-store-api), [report bugs](https://github.com/logicalclocks/feature-store-api/issues) and [add features to our library](https://github.com/logicalclocks/feature-store-api) anytime.  

We’re the best in what we do and want our community to succeed as well.  
Our many thanks to the top contributors of Hopsworks!


```sh
   cd hopsworks/hopsworks-IT/src/test/ruby/
   bundle install
   rspec --format html --out ../target/test-report.html
```
To run a single test
```sh
   cd hopsworks/hopsworks-IT/src/test/ruby/
   rspec ./spec/session_spec.rb:60
```
To skip tests that need to run inside a vm
```sh
   cd hopsworks/hopsworks-IT/src/test/ruby/
   rspec --format html --out ../target/test-report.html --tag ~vm:true
```
When the test is done if `LAUNCH_BROWSER` is set to true in `.env`, it will open the test report in a browser.

## HEAP
1. Storage setup - install
  * Create partitions on disks - `parted` is one posibility (https://www.thegeekdiary.com/how-to-create-a-partition-using-parted-command/)
  ```
  parted /dev/vdb
  (parted) mklabel msdos 
  (parted) mkpart
  Partition type? primary/extended? primary 
  File system type?  [ext2]? ext4
  Start? 0
  End? 1000G
  Warning: The resulting partition is not properly aligned for best performance.
  Ignore/Cancel? I
  ```
  * Format
  ```
   mkfs.ext4 /dev/vdb1
  ```
  * Mount
  ```
  mkdir /mnt/disk1
  mount /dev/vdb1 /mnt/disk1
  ```
  * Append to /etc/fstab
  ```
  /dev/vdb1  /srv    ext4    defaults        0 0
  ```
  * Generic install setup
  ```
  sudo yum install -y java-1.8.0-openjdk
  sudo yum install -y wget
  ssh-keygen -t rsa -N "" -f ~/.ssh/id_rsa 
  cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
  ```
  * Add each of the nodes to the other's `/etc/hosts` file
  * Karamel
  ```
  wget https://repo.hops.works/master/karamel-0.6.tgz
  tar xzvf karamel-0.6.tgz
  cd karamel-0.6
  nohup ./bin/karamel --headless &
  ```
2. Install/Restart fix `/etc/hosts` - make hostname resolv to private ip instead of localhost
3. when restarting a vm remember to
  * update main node `/srv/hops/hadoop/etc/hadoop/yarn_exclude_nodes.xml`
  * `su rmyarn` and `/srv/hops/hadoop/bin/yarn rmadmin -refreshNodes`
  * update worker ip in main node `/etc/hosts` and `systemctl restart dnsmasq`
  * shutdown all services
  * update ip in `/etc/hosts` so it doesn't use localhost, but the private ip for resolving localhost
  * update ip in `/etc/resolv.conf` - (might have to remove immutable flag - `chattr -i /etc/resolv.conf`)
  * update ip in `/etc/systemd/system/multi-user.target.wants/consul.service`
  * update ip in `/etc/dnsmasq.d/default`
  * update ip in `/srv/hops/kagent/etc/config.ini`
  * `systemctl restart dnsmasq`
  * `systemctl daemon-reload`
  * restart all services
4. decomission yarn nodes
  * add node to `/srv/hops/hadoop/etc/hadoop/yarn_exclude_nodes.xml`
  * `<host><name>heap-worker.novalocal</name></host>`
  * as user `rmyarn` run: `/srv/hops/hadoop/bin/yarn rmadmin -refreshNodes`
  * shutdown decomissioned node
5. changing hostname
  * add new host in the hopsworks.hosts table
  * update hostname in `/srv/hops/consul/.bashrc`
  * update host-id and hostname in `/srv/hops/kagent/etc/config.ini`
  * update hosts in `/srv/hops/kagent/etc/state_store/crypto_material_state.json`
  * save `/srv/hops/super_crypto` and then empty to regenerate certs
  * export cert pass `export HOPSIFY_PASSWORD=`
  * run hopsify for all users: `/srv/hops/kagent/host-certs/hopsify --config /srv/hops/kagent/etc/config.ini x509 --alt-url=https://192.168.1.21:8181/ --username consul`
  * set acls for all users `setfacl -m u:consul:rx /srv/hops/super_crypto/consul`
  * remove `data_dir` from consul before restarting it to avoid id clashes
