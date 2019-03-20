# Openshift - Httpd Proxy with Tomcat Embedded and mod_cluster via Spring Boot
This repository contains a mod_cluster enabled and pre-configured Httpd and Tomcat instance. The *quick start* section exposes how to quickly deploy them to your Openshift Server while the rest of this Readme file explains more in details how you would configure the whole thing from scratch.

## Quick Start
You can have this project up and runnning in your Openshift Server by following these quick steps:

1. Login to your Openshift Server using `oc login` and create a new project named _httpd-modcluster_
```
oc new-project httpd-modcluster
```

2. Deploy the Httpd Server using the YAML script provided
```
oc create -f deploy-project.yaml
```

3. Build and deploy the Tomcat Server using the maven target `fabric8:deploy`
```
mvn fabric8:deploy
```

Then simply connect to the UI, create a route for the httpd-modcluster service and you're good to go !

## Enabling mod_cluster in Httpd
### Building and Installing mod_proxy_cluster
Httpd needs four additional libraries to run mod_cluster:
* mod_cluster_slotmen
* mod_manager
* mod_proxy_cluster
* mod_advertise

These libraries can be built at [modcluster/mod_proxy_cluster](https://github.com/modcluster/mod_proxy_cluster). To install them, simply copy the resulting artifacts (*.so) to your Httpd installation directory _{HOME_HOME}/lib_.

### Configuring Httpd
Now that the libraries are installed, add the corresponding lines in httpd.conf to load them at launch:
```
LoadModule cluster_slotmem_module modules/mod_cluster_slotmem.so
LoadModule manager_module modules/mod_manager.so
LoadModule proxy_cluster_module modules/mod_proxy_cluster.so
LoadModule advertise_module modules/mod_advertise.so
```
This project is using the AJP connector for Httpd to Tomcat communication. You will therefore also have to enable mod_proxy_ajp by decommanting the corresponding line in the configuration:
```
LoadModule proxy_ajp_module modules/mod_proxy_ajp.so
```

Finally, edit the default manager_module virtual host in the following manner:
```
<IfModule manager_module>
  Listen *:6666
  ManagerBalancerName mycluster
  <VirtualHost *:6666>
    <Location />
     Require all granted
     #Require ip 127.0.0
    </Location>

    KeepAliveTimeout 300
    MaxKeepAliveRequests 0
    #ServerAdvertise on http://@IP@:6666
    AdvertiseFrequency 5
    #AdvertiseSecurityKey secret
    #AdvertiseGroup @ADVIP@:23364
    EnableMCPMReceive

    <Location /mod_cluster_manager>
       SetHandler mod_cluster-manager
       Require all granted
       #Require ip 127.0.0
    </Location>

  </VirtualHost>
</IfModule>
```
Note that we enabled `Require all granted` to showcase how the whole thing comes together without worring too much about access rights. A future version may tackle this problematic.

For more information on the configuration, please refer to the [mod_cluster documentation](https://docs.modcluster.io/).

### Httpd deployment Script
The deployment script creates an Openshift deployment and service resource. The important elements of this script is to define both 8080 and 6666 port as `containerPort` in the service:

```
spec:
  containers:
    ports:
    - containerPort: 8080
      protocol: TCP
    - containerPort: 6666
      protocol: TCP
      name: modcluster
```

And also in the `port` section of the service:

```
spec:
  ports: 
  - name: http
    port: 8080
    protocol: TCP
    targetPort: 8080
  - name: modcluster
    port: 6666
    protocol: TCP
```

### Spring-Boot Web Starter
Our project uses mod_cluster modules version 2.0.0-Alpha1-SNAPSHOT as dependencies. To get them, you'll have to build them from [their master branch](https://github.com/modcluster/mod_cluster) and add them to your local Maven.
