# Httpd Proxy with Tomcat Embedded and mod_cluster via Spring Boot

## Building and Installing mod_proxy_cluster
Httpd needs four additional libraries to run mod_cluster:
* mod_cluster_slotmen
* mod_manager
* mod_proxy_cluster
* mod_advertise

These libraries can be built at [modcluster/mod_proxy_cluster](https://github.com/modcluster/mod_proxy_cluster). To install them, simply copy the resulting artifacts (*.so) to your Httpd installation directory _{HOME_HOME}/lib_.

## Configuring Httpd
Now that the libraries are installed, add the corresponding lines in httpd.conf to load them at launch:
```
LoadModule cluster_slotmem_module modules/mod_cluster_slotmem.so
LoadModule manager_module modules/mod_manager.so
LoadModule proxy_cluster_module modules/mod_proxy_cluster.so
LoadModule advertise_module modules/mod_advertise.so
```
Finally, add the following virtual host:

```
<IfModule manager_module>
  Listen 127.0.0.1:6666
  ManagerBalancerName mycluster
  <VirtualHost 127.0.0.1:6666>
    <Location />
     Order deny,allow
     Allow from all
     Require ip 127.0.0
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
       Require ip 127.0.0
    </Location>

  </VirtualHost>
</IfModule>
```
Note that part of this code is available by default in _httpd.conf_.
For more information on the configuration, please refer to the [mod_cluster documentation](https://docs.modcluster.io/).

## Building and running Spring-Boot
Our project uses mod_cluster modules version 2.0.0-Alpha1-SNAPSHOT as dependencies. To get them, you'll have to build them from [their master branch](https://github.com/modcluster/mod_cluster) and add them to your local Maven.

You now have everything needed to you launch the Spring-Boot Embedded Tomcat. TO do so, simply run the following command in the root directory of this project:
```
$ mvn spring-boot:run
```
Accessing your Httpd server via an HTTP request should now display our sample application.